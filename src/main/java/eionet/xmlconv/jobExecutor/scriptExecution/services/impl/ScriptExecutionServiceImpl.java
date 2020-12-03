package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Service
public class ScriptExecutionServiceImpl implements ScriptExecutionService {

    Script script;

    @Autowired
    public ScriptExecutionServiceImpl() {
    }

    public ScriptExecutionServiceImpl(Script script) {
        script = script;
    }

    /**
     * Result of the Script
     * @throws ScriptExecutionException If an error occurs.
     */
    @Override
    public String getResult() throws ScriptExecutionException {
        initEngine();
        return script.getEngine().getResult(script);
    }

    /**
     * Gets result
     * @param out Output Stream
     * @throws ScriptExecutionException If an error occurs.
     */
    @Override
    public void getResult(OutputStream out) throws ScriptExecutionException {
        initEngine();
        script.getEngine().getResult(script, out);
    }

    /**
     * Initializes QA engine
     * @throws ScriptExecutionException If an error occurs.
     */
    private void initEngine() throws ScriptExecutionException {

        if (script.getEngine() == null) {
            try {
                if (Script.SCRIPT_LANG_XSL.equals(script.getScriptType())) {
                    script.setEngine(new XslEngineServiceImpl()) ;
                } else if (Script.SCRIPT_LANG_XGAWK.equals(script.getScriptType())) {
                    script.setEngine(new XGawkQueryEngineServiceImpl()) ;
                } else if (Script.SCRIPT_LANG_FME.equals(script.getScriptType())) {
                    script.setEngine(new FMEQueryEngineServiceImpl()) ;
                } else if (Script.SCRIPT_LANG_XQUERY3.equals(script.getScriptType())) {
                    // XQUERY 3.0+
                    script.setEngine(new BaseXLocalEngineServiceImpl()) ;
                } else {
                    // LEGACY XQUERY 1.0
                    script.setEngine(new SaxonEngineServiceImpl()) ;
                }
            } catch (Exception e) {
                throw new ScriptExecutionException("Error initializing engine  " + e.toString());
            }
        }
    }

}
