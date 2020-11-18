package eionet.xmlconv.jobExecutor.rabbitmq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSenderImpl implements RabbitMQSender {

    @Value("${job.rabbitmq.exchange}")
    private String exchange;
    @Value("${job.rabbitmq.routingKey}")
    private String routingKey;

    RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQSenderImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        System.out.println("sent message " + message);
    }
}














