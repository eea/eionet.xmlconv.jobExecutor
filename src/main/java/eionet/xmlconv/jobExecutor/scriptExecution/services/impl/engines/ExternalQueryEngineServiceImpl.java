package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.Map;

@Service
public abstract class ExternalQueryEngineServiceImpl extends ScriptEngineServiceImpl{
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalQueryEngineServiceImpl.class);

    @Autowired
    public ExternalQueryEngineServiceImpl() {
    }

    /**
     * Gets shell command
     * @param dataFile data file
     * @param scriptFile script file
     * @param params parameters
     * @return
     */
    protected abstract String getShellCommand(String dataFile, String scriptFile, Map<String, String> params);

    @Override
    protected void runQuery(Script script, OutputStream result) throws ScriptExecutionException {
        //TODO
      /*  String tmpScriptFile = null;
        HttpFileManagerService fileManager = new HttpFileManagerServiceImpl();
        try {

            // build InputSource for xsl
            if (!Utils.isNullStr(script.getScriptSource())) {
                tmpScriptFile = Utils.saveStrToFile(null, script.getScriptSource(), "xgawk");
                script.setScriptFileName(tmpScriptFile);
            } else if (!Utils.isNullStr(script.getScriptFileName())) {
                // fisXsl=new FileInputStream(script.getScriptFileName());
            } else {
                throw new ScriptExecutionException("XQuery engine could not find script source or script file name!");
            }

            InputStream sourceStream = fileManager.getFileInputStream(script.getSrcFileUrl(), null, false);
            String srcFile = CustomFileUtils.saveFileInLocalStorage(sourceStream, "xml");

            String originSourceUrl = script.getOrigFileUrl();
            LOGGER.info("Original Script Source URL:"+originSourceUrl);
            Map<String, String> params = UrlUtils.getCdrParams(originSourceUrl);
            params.put(Constants.XQ_SOURCE_PARAM_NAME, script.getOrigFileUrl());

            String cmd = getShellCommand(srcFile, script.getScriptFileName(), params);

            LOGGER.debug("Execute command: " + cmd);

            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(cmd);
            LOGGER.debug("Exit status: " + exitStatus);

            String cmdError = cmdExecutor.getCommandError();
            LOGGER.debug("Command error: " + cmdError);

            String cmdOutput = cmdExecutor.getCommandOutput();
            // _logger.debug("Command output: " + cmdOutput);
            boolean throwError = false;

            if (Utils.isNullStr(cmdOutput) && !Utils.isNullStr(cmdError)) {
                Streams.drain(new StringReader(cmdError), result);
                throwError = true;
            } else {
                Streams.drain(new StringReader(cmdOutput), result);
            }

            // clean tmp files
            if (tmpScriptFile != null) {
                Utils.deleteFile(tmpScriptFile);
            }
            if (srcFile != null) {
                Utils.deleteFile(srcFile);
            }
            if (throwError) {
                throw new ScriptExecutionException(cmdError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("==== Caught EXCEPTION " + e.toString());
            throw new ScriptExecutionException(e.getMessage());
        } finally {
            fileManager.closeQuietly();
            try {
                result.close();
                result.flush();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }
        }
*/
    }
}
