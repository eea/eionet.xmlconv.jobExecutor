package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StatusInitializer {

    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusInitializer.class);

    public StatusInitializer(RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever) {
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeWorkerStatusAfterStartup() {
        LOGGER.info(String.format("Container name is %s", containerInfoRetriever.getContainerName()));
        WorkerStateRabbitMQResponse response = new WorkerStateRabbitMQResponse(containerInfoRetriever.getContainerName(), Constants.WORKER_READY).setHeartBeatQueue(RabbitMQConfig.queue);
        rabbitMQSender.sendWorkerStatus(response);
        LOGGER.info("Message for initializing JobExecutor status sent on rabbitmq");
    }
}
