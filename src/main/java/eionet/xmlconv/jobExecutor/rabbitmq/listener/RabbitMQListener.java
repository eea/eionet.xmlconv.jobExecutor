package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(String message) {
        System.out.println("received message: " + message);
    }
}
