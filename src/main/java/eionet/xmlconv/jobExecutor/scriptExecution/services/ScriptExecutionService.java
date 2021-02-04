package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;

public interface ScriptExecutionService {
    void setScript(Script script);
    String getResult() throws ScriptExecutionException;
}
