package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.objects.EnvironmentVar;
import eionet.xmlconv.jobExecutor.scriptExecution.services.LogDeviceService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.SysCommandExecutorService;
import eionet.xmlconv.jobExecutor.utils.AsyncStreamReader;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import eionet.xmlconv.jobExecutor.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Usage of following class can go as ...
 *
 * <PRE>
 * <CODE>
 * 		SysCommandExecutor cmdExecutor = new SysCommandExecutor();
 * 		cmdExecutor.setOutputLogDevice(new LogDevice());
 * 		cmdExecutor.setErrorLogDevice(new LogDevice());
 * 		int exitStatus = cmdExecutor.runCommand(commandLine);
 * </CODE>
 * </PRE>
 *
 *
 * OR
 *
 *
 * <PRE>
 * <CODE>
 * 		SysCommandExecutor cmdExecutor = new SysCommandExecutor();
 * 		int exitStatus = cmdExecutor.runCommand(commandLine);
 *
 * 		String cmdError = cmdExecutor.getCommandError();
 * 		String cmdOutput = cmdExecutor.getCommandOutput();
 * </CODE>
 * </PRE>
 *
 */
@Service
public class SysCommandExecutorServiceImpl implements SysCommandExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysCommandExecutorService.class);

    private LogDeviceService fOuputLogDevice = null;
    private LogDeviceService fErrorLogDevice = null;
    private String fWorkingDirectory = null;
    private List<EnvironmentVar> fEnvironmentVarList = null;

    private StringBuffer fCmdOutput = null;
    private StringBuffer fCmdError = null;
    private AsyncStreamReader fCmdOutputThread = null;
    private AsyncStreamReader fCmdErrorThread = null;

    private long timeout = 0;

    @Autowired
    public SysCommandExecutorServiceImpl() {
    }

    /**
     * Gets timeout
     * @return Timeout
     */
    @Override
    public long getTimeout() {
        if (timeout == 0) {
            timeout = Properties.qaTimeout;
        }
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setOutputLogDevice(LogDeviceService logDevice) {
        fOuputLogDevice = logDevice;
    }

    public void setErrorLogDevice(LogDeviceService logDevice) {
        fErrorLogDevice = logDevice;
    }

    public void setWorkingDirectory(String workingDirectory) {
        fWorkingDirectory = workingDirectory;
    }

    /**
     * Sets environment variable
     * @param name Name
     * @param value Value
     */
    @Override
    public void setEnvironmentVar(String name, String value) {
        if (fEnvironmentVarList == null) {
            fEnvironmentVarList = new ArrayList<EnvironmentVar>();
        }

        fEnvironmentVarList.add(new EnvironmentVar(name, value));
    }

    @Override
    public String getCommandOutput() {
        return fCmdOutput.toString();
    }

    @Override
    public String getCommandError() {
        return fCmdError.toString();
    }

    /**
     * Runs command.
     * @param commandLine Command
     * @return Result
     * @throws Exception If an error occurs.
     */
    @Override
    public int runCommand(String commandLine) throws Exception {
        /* run command */
        Process process = runCommandHelper(commandLine);

        /* start output and error read threads */
        startOutputAndErrorReadThreads(process.getInputStream(), process.getErrorStream());

        // create and start a Worker thread which this thread will join for the timeout period
        Worker worker = new Worker(process);
        worker.start();
        try {
            worker.join(getTimeout());
            Integer exitValue = worker.getExitValue();
            if (exitValue != null) {
                // the worker thread completed within the timeout period
                return exitValue;
            }

            // if we get this far then we never got an exit value from the worker thread as a result of a timeout
            String errorMessage = "The command [" + commandLine + "] timed out.";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw ex;
        }

    }

    /**
     * Runs command helper.
     * @param commandLine Command
     * @return Result
     * @throws IOException If an error occurs.
     */
    private Process runCommandHelper(String commandLine) throws IOException {
        Process process = null;
        commandLine = validateSystemAndMassageCommand(commandLine);
        if (fWorkingDirectory == null) {
            process = Runtime.getRuntime().exec(commandLine, getEnvTokens());
        } else {
            process = Runtime.getRuntime().exec(commandLine, getEnvTokens(), new File(fWorkingDirectory));
        }

        return process;
    }

    /**
     * Starts output and error read threads.
     * @param processOut Output InputStream
     * @param processErr Error InputStream
     */
    private void startOutputAndErrorReadThreads(InputStream processOut, InputStream processErr) {
        fCmdOutput = new StringBuffer();
        fCmdOutputThread = new AsyncStreamReader(processOut, fCmdOutput, fOuputLogDevice, "OUTPUT");
        fCmdOutputThread.start();

        fCmdError = new StringBuffer();
        fCmdErrorThread = new AsyncStreamReader(processErr, fCmdError, fErrorLogDevice, "ERROR");
        fCmdErrorThread.start();
    }

    /**
     * Notify Output and error read threads to stop reading.
     */
    private void notifyOutputAndErrorReadThreadsToStopReading() {
        fCmdOutputThread.stopReading();
        fCmdErrorThread.stopReading();
    }

    /**
     * Gets environment tokens
     * @return Tokens array
     */
    private String[] getEnvTokens() {
        if (fEnvironmentVarList == null) {
            return null;
        }

        String[] envTokenArray = new String[fEnvironmentVarList.size()];
        Iterator<EnvironmentVar> envVarIter = fEnvironmentVarList.iterator();
        int nEnvVarIndex = 0;
        while (envVarIter.hasNext()) {
            EnvironmentVar envVar = (envVarIter.next());
            String envVarToken = envVar.fName + "=" + envVar.fValue;
            envTokenArray[nEnvVarIndex++] = envVarToken;
        }

        return envTokenArray;
    }

    /**
     * Validates that the system is running a supported OS and returns a system-appropriate command line.
     *
     * @param originalCommand Original command
     * @return Command message
     */
    private static String validateSystemAndMassageCommand(final String originalCommand) {
        // make sure that we have a command
        if (Utils.isNullStr(originalCommand) || (originalCommand.length() < 1)) {
            String errorMessage = "Missing or empty command line parameter.";
            throw new RuntimeException(errorMessage);
        }

        // make sure that we are running on a supported system, and if so set the command line appropriately
        String massagedCommand;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            massagedCommand = "cmd.exe /C " + originalCommand;
        } else if (osName.equals("Solaris") || osName.equals("SunOS") || osName.equals("Linux")) {
            massagedCommand = originalCommand;
        } else {
            String errorMessage =
                    "Unable to run on this system which is not Solaris, Linux, or some Windows (actual OS type: \'" + osName
                            + "\').";
            throw new RuntimeException(errorMessage);
        }

        return massagedCommand;
    }

    /**
     * Thread class to be used as a worker
     */
    private static class Worker extends Thread {
        private final Process process;
        private Integer exitValue;

        /**
         * Constructor
         * @param process Process
         */
        Worker(final Process process) {
            this.process = process;
        }

        public Integer getExitValue() {
            return exitValue;
        }

        @Override
        public void run() {
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException ignore) {
                return;
            }
        }
    }
}
