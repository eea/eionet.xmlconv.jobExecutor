package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponse;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq
     * @param response
     */
    void sendMessage(WorkerJobInfoRabbitMQResponse response);

    void sendWorkerStatus(WorkerStateRabbitMQResponse response);

}
