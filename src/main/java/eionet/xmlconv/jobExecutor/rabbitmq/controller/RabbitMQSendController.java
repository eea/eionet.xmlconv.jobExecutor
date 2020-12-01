package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import eionet.xmlconv.jobExecutor.rabbitmq.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rabbitmq")
public class RabbitMQSendController {

    RabbitMQSender rabbitMQSender;

    @Autowired
    ContainerInfoRetriever containerInfoRetriever;

    @Autowired
    public RabbitMQSendController(RabbitMQSender rabbitMQSender) {
        this.rabbitMQSender = rabbitMQSender;
    }

    @PostMapping("/send/{message}")
    public String sendMessage(@PathVariable String message) {
        rabbitMQSender.sendMessage(message);
        return "message sent";
    }

    @GetMapping("/info")
    public String getInfo() {
        System.out.println("inside method test");
        Object result = containerInfoRetriever.getContainerId();
        System.out.println(result);
        return "info retrieved";
    }
}
