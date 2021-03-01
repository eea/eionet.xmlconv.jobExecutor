package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkersRabbitMQResponse;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq
     * @param response
     */
    void sendMessage(WorkersRabbitMQResponse response);

    void sendOnDemandMessage(WorkersRabbitMQResponse response);
}
