package eionet.xmlconv.jobExecutor.rabbitmq.service;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq
     * @param message
     */
    void sendMessage(String message);
}
