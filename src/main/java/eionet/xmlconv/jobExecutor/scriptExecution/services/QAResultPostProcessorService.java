package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Schema;

public interface QAResultPostProcessorService {
    String processQAResult(String result, Schema xmlSchema) throws ScriptExecutionException;
    String processQAResult(String result, String xmlSchemaUrl) throws ScriptExecutionException;
    String getWarningMessage(String xmlSchemaUrl);
}
