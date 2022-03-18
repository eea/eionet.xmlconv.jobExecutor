package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;

import java.io.IOException;

public interface FmeQueryAsynchronousHandler {

    void pollFmeServerForResults(Script script, String folderName) throws IOException, DatabaseException;

    void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponseMessage response);
}
