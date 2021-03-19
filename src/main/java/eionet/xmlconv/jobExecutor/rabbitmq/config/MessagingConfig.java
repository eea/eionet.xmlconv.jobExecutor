package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MessagingConfig {

    @Value("${heartBeat.request.rabbitmq.exchange}")
    private String exchange;

    private ContainerInfoRetriever containerInfoRetriever;
    public static String queue;

    @Autowired
    public MessagingConfig(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;

    }

    @Bean
    public Queue queue() {
        queue = containerInfoRetriever.getContainerName() + "-queue";
        return new Queue(queue);
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
