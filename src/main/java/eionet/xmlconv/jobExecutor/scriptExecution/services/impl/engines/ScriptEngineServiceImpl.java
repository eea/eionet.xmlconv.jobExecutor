package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.QAResultPostProcessorService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptEngineService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.QAResultPostProcessorServiceImpl;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
    protected abstract void runQuery(Script script, OutputStream result) throws Exception;

    @Override
    public void getResult(Script script, OutputStream out) throws ScriptExecutionException {
        try {
            setOutputType(script.getOutputType());
            runQuery(script, out);
        } catch (Exception e) {
            throw new ScriptExecutionException(e.getMessage(), e);
        }
    }


    @Override
    public String getResult(Script script) throws ScriptExecutionException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        String res = "";
        getResult(script, result);
        try {
            res = result.toString(DEFAULT_ENCODING);
        } catch (Exception e) {
            LOGGER.error("Error while converting QA result" + e);
        }
        // add "red coloured warning" if script is expired
        if (script.getOutputType().equals(Script.SCRIPT_RESULTTYPE_HTML) && script.getSchema() != null) {
            QAResultPostProcessorService postProcessor = new QAResultPostProcessorServiceImpl();
            res = postProcessor.processQAResult(res, script.getSchema());
        }
        return res;
    }

    @Override
    public String getEncoding() {
        if (Utils.isNullStr(encoding)) {
            encoding = DEFAULT_ENCODING;
        }

        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
