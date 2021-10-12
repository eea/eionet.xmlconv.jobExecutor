package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq containing job results
     * @param response
     */
    void sendMessage(WorkerJobInfoRabbitMQResponseMessage response);

    /**
     * sends message to rabbitmq containing jobExecutor status
     * @param response
     */
    void sendWorkerStatus(WorkerStateRabbitMQResponseMessage response);

    /**
     * sends message to rabbitmq containing information of whether jobExecutor is
     * executing a specific job
     * @param response
     */
    void sendHeartBeatMessageResponse(WorkerHeartBeatMessage response);

    /**
     * sends message to dead letter queue which contains the script and the error message
     * @param message
     */
    void sendMessageToDeadLetterQueue(WorkerJobRabbitMQRequestMessage message);

}
