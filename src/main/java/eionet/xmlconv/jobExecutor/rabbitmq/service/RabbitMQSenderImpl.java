package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessageInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequest;
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
    @Value("${heartBeat.response.rabbitmq.routingKey}")
    private String heartBeatResponseRoutingKey;

    @Value("${rabbitmq.dead.letter.exchange}")
    private String deadLetterExchange;
    @Value("${rabbitmq.dead.letter.routingKey}")
    private String deadLetterRoutingKey;

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

    @Override
    public void sendHeartBeatMessageResponse(WorkerHeartBeatMessageInfo response) {
        rabbitTemplate.convertAndSend(exchange, heartBeatResponseRoutingKey, response);
    }

    @Override
    public void sendMessageToDeadLetterQueue(WorkerJobRabbitMQRequest message) {
        rabbitTemplate.convertAndSend(deadLetterExchange, deadLetterRoutingKey, message);
    }
}














