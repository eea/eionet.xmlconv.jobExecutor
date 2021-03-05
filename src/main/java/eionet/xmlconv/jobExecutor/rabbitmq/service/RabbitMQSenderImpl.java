package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkersRabbitMQResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSenderImpl implements RabbitMQSender {

    @Value("${job.rabbitmq.jobsResultExchange}")
    private String exchange;
    @Value("${job.rabbitmq.jobsResultRoutingKey}")
    private String routingKey;

    RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQSenderImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMessage(WorkersRabbitMQResponse response) {
        rabbitTemplate.convertAndSend(exchange, routingKey, response);
    }
}














