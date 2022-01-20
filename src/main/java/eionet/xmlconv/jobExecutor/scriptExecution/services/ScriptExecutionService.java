package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;

public interface ScriptExecutionService {
    void setScript(Script script);
    void getResult(WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException;
}
