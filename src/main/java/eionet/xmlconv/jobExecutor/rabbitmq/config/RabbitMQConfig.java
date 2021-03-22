package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.listener.HeartBeatMessageListener;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMQConfig {

    @Value("${heartBeat.request.rabbitmq.exchange}")
    private String exchange;
    @Value("${heartBeat.request.message.time.expiration}")
    private Integer MESSAGE_TIME_EXPIRATION;

    private ContainerInfoRetriever containerInfoRetriever;
    public static String queue;

    @Autowired
    public RabbitMQConfig(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;

    }

    @Bean
    public Queue queue() {
        queue = containerInfoRetriever.getContainerName() + "-queue";
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
        return rabbitTemplate;
    }

    @Bean
    public Constants constants(){
        return new Constants();
    }
}
