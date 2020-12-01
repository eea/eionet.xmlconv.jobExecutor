package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rabbitmq")
public class RabbitMQSendController {

    RabbitMQSender rabbitMQSender;

    @Autowired
    public RabbitMQSendController(RabbitMQSender rabbitMQSender) {
        this.rabbitMQSender = rabbitMQSender;
    }

    @PostMapping("/send/{message}")
    public String sendMessage(@PathVariable String message) {
        rabbitMQSender.sendMessage(message);
        return "message sent";
    }
}
