package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;

import java.io.IOException;

public interface FmeQueryAsynchronousHandler {

    void pollFmeServerForResults(Script script, String folderName) throws IOException, DatabaseException, GenericFMEexception, RetryCountForGettingJobResultReachedException, InterruptedException, FmeAuthorizationException, FMEBadRequestException, FmeCommunicationException;
    void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponseMessage response);

}
