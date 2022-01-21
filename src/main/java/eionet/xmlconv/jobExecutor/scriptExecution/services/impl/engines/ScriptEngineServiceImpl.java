package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.scriptExecution.services.QAResultPostProcessorService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptEngineService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.QAResultPostProcessorServiceImpl;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;

@Service
public abstract class ScriptEngineServiceImpl implements ScriptEngineService {


    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptEngineServiceImpl.class);
    private String encoding = null;
    private String outputType = null;

    @Autowired
    public ScriptEngineServiceImpl() {
    }

    /**
     * Runs query
     * @param script Script to run
     * @param result Result
     * @throws Exception If an error occurs.
     */
    protected abstract void runQuery(Script script, OutputStream result,  WorkerJobInfoRabbitMQResponseMessage response) throws Exception;

    @Override
    public void getResult(Script script, OutputStream out, WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException {
        try {
            setOutputType(script.getOutputType());
            runQuery(script, out, response);
            String message = "For job id " + script.getJobId() + " the script ";
            if(!Utils.isNullStr(script.getScriptFileName())){
                message+= script.getScriptFileName() + " file ";
            }
            message += "was executed.";
            LOGGER.info(message);
        } catch (Exception e) {
            String message = "For job id " + script.getJobId() + " the script ";
            if(!Utils.isNullStr(script.getScriptFileName())){
                message+= script.getScriptFileName() + " file ";
            }
            message += " was executed unsuccessfully. Exception message is: " + e.getMessage();
            LOGGER.error(message);
            throw new ScriptExecutionException(e.getMessage(), e);
        }
    }


    @Override
    public void getResult(Script script, WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException {
        FileOutputStream result = null;
        try {
            result = new FileOutputStream(new File(script.getStrResultFile()));
        } catch (FileNotFoundException e) {
            throw new ScriptExecutionException("For job id " + script.getJobId() + " could not find result file " + script.getStrResultFile());
        }
        try {
            getResult(script, result, response);
        } catch (ScriptExecutionException see) {
            LOGGER.error("For job id " + script.getJobId() + " could not execute getResult method. Exception message is: " + see.getMessage() );
            StringBuilder errBuilder = new StringBuilder();
            errBuilder.append("<div class=\"feedbacktext\"><span id=\"feedbackStatus\" class=\"BLOCKER\" style=\"display:none\">Unexpected error occured!</span><h2>Unexpected error occured!</h2>");
            errBuilder.append(Utils.escapeXML(see.toString()));
            errBuilder.append("</div>");
            try {
                IOUtils.write(errBuilder.toString(), result, "UTF-8");
            } catch (IOException ex) {
                throw new ScriptExecutionException(see.getMessage() + " " + ex.getMessage());
            }
            throw see;
        }

    }

    @Override
    public String getOutputType() {
        if (Utils.isNullStr(outputType)) {
            outputType = DEFAULT_OUTPUTTYPE;
        }
        return outputType;
    }

    @Override
    public void setOutputType(String outputType) {
        outputType = (outputType == null) ? DEFAULT_OUTPUTTYPE : outputType.trim().toLowerCase();
        outputType = (outputType.equals("txt")) ? "text" : outputType;

        if (outputType.equals("xml") || outputType.equals("html") || outputType.equals("text") || outputType.equals("xhtml")) {
            this.outputType = outputType;
        } else {
            this.outputType = DEFAULT_OUTPUTTYPE;
        }
    }

    /**
     * Parses parameters
     * @param xqParams xquery parameters
     * @return Parameter map
     * @throws ScriptExecutionException If an error occurs.
     */
    public HashMap parseParams(String[] xqParams) throws ScriptExecutionException {
        HashMap<String, String> paramsMap = new HashMap<String, String>();

        if (xqParams != null) {
            for (int p = 0; p < xqParams.length; p++) {
                String arg = xqParams[p];
                int eq = arg.indexOf("=");
                if (eq < 1 || eq >= arg.length() - 1) {
                    throw new ScriptExecutionException("Bad param=value pair");
                    // handleError("Bad param=value pair", true);
                }
                String argname = arg.substring(0, eq);
                paramsMap.put(argname, arg.substring(eq + 1));
            }

        }
        return paramsMap;
    }

}
