package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
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
    public void getResult(WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException {
        if (Script.SCRIPT_LANG_XSL.equals(script.getScriptType())) {
            xslEngineService.getResult(script, response);
        } else if (Script.SCRIPT_LANG_FME.equals(script.getScriptType())) {
            fmeEngineService.getResult(script, response);
        } else if (Script.SCRIPT_LANG_XQUERY3.equals(script.getScriptType())) {// XQUERY 3.0+
            basexEngineService.getResult(script, response);
        } else { // LEGACY XQUERY 1.0
            saxonEngineService.getResult(script, response);
        }

    }

}
