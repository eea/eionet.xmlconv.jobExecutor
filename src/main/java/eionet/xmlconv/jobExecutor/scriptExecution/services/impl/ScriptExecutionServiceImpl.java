package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptEngineService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class ScriptExecutionServiceImpl implements ScriptExecutionService {

    Script script;

    @Autowired
    @Qualifier("xslEngineService")
    private ScriptEngineService xslEngineService;

    @Autowired
    @Qualifier("basexEngineService")
    private ScriptEngineService basexEngineService;

    @Autowired
    @Qualifier("xgawkEngineService")
    private ScriptEngineService xgawkEngineService;

    @Autowired
    @Qualifier("saxonEngineService")
    private ScriptEngineService saxonEngineService;

    @Autowired
    @Qualifier("fmeEngineService")
    private ScriptEngineService fmeEngineService;

    @Autowired
    public ScriptExecutionServiceImpl(){
    }

    @Override
    public void setScript(Script script) {
        this.script = script;
    }

    /**
     * Result of the Script
     * @throws ScriptExecutionException If an error occurs.
     */
    @Override
    public String getResult() throws ScriptExecutionException {
        String result = null;
        if (Script.SCRIPT_LANG_XSL.equals(script.getScriptType())) {
            result = xslEngineService.getResult(script);
        } else if (Script.SCRIPT_LANG_XGAWK.equals(script.getScriptType())) {
            result = xgawkEngineService.getResult(script);
        } else if (Script.SCRIPT_LANG_FME.equals(script.getScriptType())) {
            result = fmeEngineService.getResult(script);
        } else if (Script.SCRIPT_LANG_XQUERY3.equals(script.getScriptType())) {// XQUERY 3.0+
            result = basexEngineService.getResult(script);
        } else { // LEGACY XQUERY 1.0
            result = saxonEngineService.getResult(script);
        }
        return result;

    }

}
