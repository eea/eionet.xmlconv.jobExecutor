package eionet.xmlconv.jobExecutor.rabbitmq.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.enums.RabbitmqMessageType;
import eionet.xmlconv.jobExecutor.rabbitmq.listener.HeartBeatMessageListener;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@ConditionalOnProperty(
        value="rabbitmq.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Configuration
public class RabbitMQConfig {

    @Value("${heartBeat.request.rabbitmq.exchange}")
    private String exchange;
    @Value("${heartBeat.request.message.time.expiration}")
    private Integer MESSAGE_TIME_EXPIRATION;
    @Value("${job.rabbitmq.jobsResultRoutingKey}")
    private String jobResultsRoutingKey;
    @Value("${heartBeat.response.rabbitmq.routingKey}")
    private String heartBeatResponseRoutingKey;
    @Value("${rabbitmq.dead.letter.routingKey}")
    private String deadLetterRoutingKey;
    @Value("${job.rabbitmq.workerStatusRoutingKey}")
    private String workerStatusRoutingKey;

    private ContainerInfoRetriever containerInfoRetriever;
    public static String queue;
    public static volatile Map<Message, Integer> rabbitmqRetries = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConfig.class);
    private static final Integer MAX_RABBITMQ_RETRIES = 3;

    @Autowired
    public RabbitMQConfig(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;

    }

    @Bean
    public Queue queue() {
        queue = containerInfoRetriever.getContainerName() + "-heartbeat-queue";
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", MESSAGE_TIME_EXPIRATION);
        return new Queue(queue, true , false, false , args);
    }

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(exchange);
    }

    @Bean
    public Binding binding(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory ) {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueues(queue());
        simpleMessageListenerContainer.setMessageListener(heartBeatListenerAdapter());
        return simpleMessageListenerContainer;
    }
    @Bean
    HeartBeatMessageListener heartBeatMessageReceiver() {
        return new HeartBeatMessageListener();
    }
    @Bean
    MessageListenerAdapter heartBeatListenerAdapter() {
        return new MessageListenerAdapter(heartBeatMessageReceiver(), jsonMessageConverter());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback((returnedMessage) -> {
            System.out.println("message returned");
            Message message = returnedMessage.getMessage();
            String exchange = returnedMessage.getExchange();
            String routingKey = returnedMessage.getRoutingKey();
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                if (routingKey.equals(jobResultsRoutingKey)) {
                    WorkerJobInfoRabbitMQResponseMessage response = mapper.readValue(message.getBody(), WorkerJobInfoRabbitMQResponseMessage.class);
                    if (findIfMaxRetriesExceeded(message, RabbitmqMessageType.JOB_RESPONSE_MESSAGE)) {
                        return;
                    }
                    rabbitTemplate.convertAndSend(exchange, routingKey, response);
                    LOGGER.info(RabbitmqMessageType.JOB_RESPONSE_MESSAGE.getValue() + " for job " + response.getScript().getJobId() + " was sent again in routingKey " + routingKey);
                } else if (routingKey.equals(heartBeatResponseRoutingKey)) {
                    WorkerHeartBeatMessage response = mapper.readValue(message.getBody(), WorkerHeartBeatMessage.class);
                    if (findIfMaxRetriesExceeded(message, RabbitmqMessageType.HEART_BEAT_MESSAGE)) {
                        return;
                    }
                    rabbitTemplate.convertAndSend(exchange, routingKey, response);
                    LOGGER.info(RabbitmqMessageType.HEART_BEAT_MESSAGE.getValue() + " for worker " + response.getJobExecutorName() + " and job " + response.getJobId() + " was sent again in routingKey " + routingKey);
                } else if (routingKey.equals(deadLetterRoutingKey)) {
                    WorkerJobRabbitMQRequestMessage response = mapper.readValue(message.getBody(), WorkerJobRabbitMQRequestMessage.class);
                    if (findIfMaxRetriesExceeded(message, RabbitmqMessageType.DEAD_LETTER_MESSAGE)) {
                        return;
                    }
                    rabbitTemplate.convertAndSend(exchange, routingKey, response);
                    LOGGER.info(RabbitmqMessageType.DEAD_LETTER_MESSAGE.getValue() + " for job " + response.getScript().getJobId() + " was sent again in routingKey " + routingKey);
                } else if (routingKey.equals(workerStatusRoutingKey)) {
                    WorkerStateRabbitMQResponseMessage response = mapper.readValue(message.getBody(), WorkerStateRabbitMQResponseMessage.class);
                    if (findIfMaxRetriesExceeded(message, RabbitmqMessageType.WORKER_STATUS_MESSAGE)) {
                        return;
                    }
                    rabbitTemplate.convertAndSend(exchange, routingKey, response);
                    LOGGER.info(RabbitmqMessageType.WORKER_STATUS_MESSAGE.getValue() + " for worker " + response.getJobExecutorName() + " was sent again in routingKey " + routingKey);
                }
            } catch (IOException e) {
                LOGGER.error("Error sending message " + message + " in routingKey " + routingKey);
            }
        });
        return rabbitTemplate;
    }

    private boolean findIfMaxRetriesExceeded(Message message, RabbitmqMessageType rabbitmqMessageType) throws IOException {
        Integer retries = rabbitmqRetries.get(message);
        if (retries==null) {
            retries = 1;
        } else {
            retries++;
        }
        String logMessage;
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (retries >= MAX_RABBITMQ_RETRIES) {
            switch (rabbitmqMessageType) {
                case JOB_RESPONSE_MESSAGE:
                    WorkerJobInfoRabbitMQResponseMessage msg1 = mapper.readValue(message.getBody(), WorkerJobInfoRabbitMQResponseMessage.class);
                    logMessage = rabbitmqMessageType.getValue() + " for job " + msg1.getScript().getJobId() + " reached rabbitmq maximum retries";
                    break;
                case HEART_BEAT_MESSAGE:
                    WorkerHeartBeatMessage msg2 = mapper.readValue(message.getBody(), WorkerHeartBeatMessage.class);
                    logMessage = rabbitmqMessageType.getValue() + " for worker " + msg2.getJobExecutorName() + " and job " + msg2.getJobId() + " reached rabbitmq maximum retries";
                    break;
                case DEAD_LETTER_MESSAGE:
                    WorkerJobRabbitMQRequestMessage msg3 = mapper.readValue(message.getBody(), WorkerJobRabbitMQRequestMessage.class);
                    logMessage = rabbitmqMessageType.getValue() + " for job " + msg3.getScript().getJobId() + " reached rabbitmq maximum retries";
                    break;
                case WORKER_STATUS_MESSAGE:
                    WorkerStateRabbitMQResponseMessage msg4 = mapper.readValue(message.getBody(), WorkerStateRabbitMQResponseMessage.class);
                    logMessage = rabbitmqMessageType.getValue() + " for worker " + msg4.getJobExecutorName() + " reached rabbitmq maximum retries";
                    break;
                default:
                    logMessage = "Unexpected value for rabbitmq message type";
            }
            LOGGER.info(logMessage);
            return true;
        }
        rabbitmqRetries.put(message, retries);
        return false;
    }

    @Bean
    public Constants constants(){
        return new Constants();
    }
}
