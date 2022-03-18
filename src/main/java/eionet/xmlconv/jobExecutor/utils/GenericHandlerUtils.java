package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;

public class GenericHandlerUtils {

    public static WorkerJobRabbitMQRequestMessage createMessageForDeadLetterQueue(WorkerJobRabbitMQRequestMessage request,
                                                                            String errorMessage, Integer errorStatus, String containerName, Integer workerStatus){
        request.setScript(request.getScript());
        request.setErrorMessage(errorMessage);
        request.setErrorStatus(errorStatus);
        request.setJobExecutionRetries(request.getJobExecutionRetries());
        request.setJobExecutorName(containerName);
        request.setJobExecutorStatus(workerStatus);
        request.setHeartBeatQueue(RabbitMQConfig.queue);
        return request;
    }
}
