package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessageInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequest;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponse;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq containing job results
     * @param response
     */
    void sendMessage(WorkerJobInfoRabbitMQResponse response);

    /**
     * sends message to rabbitmq containing jobExecutor status
     * @param response
     */
    void sendWorkerStatus(WorkerStateRabbitMQResponse response);

    /**
     * sends message to rabbitmq containing information of whether jobExecutor is
     * executing a specific job
     * @param response
     */
    void sendHeartBeatMessageResponse(WorkerHeartBeatMessageInfo response);

    /**
     * sends message to dead letter queue which contains the script and the error message
     * @param message
     */
    void sendMessageToDeadLetterQueue(WorkerJobRabbitMQRequest message);

}
