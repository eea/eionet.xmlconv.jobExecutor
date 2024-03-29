package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.utils.GenericHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(
        value="rabbitmq.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class StatusInitializer {

    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;

    @Value("${rancher.jobExecutor.type}")
    private Integer rancherJobExecutorType;

    private JobExecutorType jobExecutorType = JobExecutorType.Unknown;
    public static String containerName;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusInitializer.class);

    public StatusInitializer(RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever) {
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeWorkerStatusAfterStartup() {
        containerName = containerInfoRetriever.getContainerName();
        LOGGER.info(String.format("Container name is %s", containerName));
        jobExecutorType = GenericHandlerUtils.getJobExecutorType(rancherJobExecutorType);
        WorkerStateRabbitMQResponseMessage response = new WorkerStateRabbitMQResponseMessage.WorkerStateRabbitMQResponseBuilder(containerName, Constants.WORKER_READY)
                .setJobExecutorType(jobExecutorType).setHeartBeatQueue(RabbitMQConfig.queue).build();
        rabbitMQSender.sendWorkerStatus(response);
        LOGGER.info("Message for initializing JobExecutor status sent on rabbitmq");
    }
}
