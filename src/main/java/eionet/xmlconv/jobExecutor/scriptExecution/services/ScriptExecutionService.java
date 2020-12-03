package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;

import java.io.OutputStream;

public interface ScriptExecutionService {
    String getResult() throws ScriptExecutionException;
    void getResult(OutputStream out) throws ScriptExecutionException;
}
