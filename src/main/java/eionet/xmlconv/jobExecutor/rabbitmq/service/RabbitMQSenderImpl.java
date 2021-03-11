package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSenderImpl implements RabbitMQSender {

    @Value("${job.rabbitmq.jobsResultExchange}")
    private String exchange;
    @Value("${job.rabbitmq.jobsResultRoutingKey}")
    private String jobResultsRoutingKey;
    @Value("${job.rabbitmq.workerStatusRoutingKey}")
    private String workerStatusRoutingKey;

    RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQSenderImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMessage(WorkerJobInfoRabbitMQResponse response) {
        rabbitTemplate.convertAndSend(exchange, jobResultsRoutingKey, response);
    }

    @Override
    public void sendWorkerStatus(WorkerStateRabbitMQResponse response) {
        rabbitTemplate.convertAndSend(exchange, workerStatusRoutingKey, response);
    }
}














