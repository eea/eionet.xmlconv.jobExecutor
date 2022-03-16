package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
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

    public static JobExecutorType jobExecutorType;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusInitializer.class);

    public StatusInitializer(RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever) {
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeWorkerStatusAfterStartup() {
        ContainerInfo containerInfo = containerInfoRetriever.getContainerInfo();
        LOGGER.info(String.format("Container name is %s", containerInfo.getName()));
        if (rancherJobExecutorType.equals(JobExecutorType.Light.getId())) {
            jobExecutorType = JobExecutorType.Light;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Heavy.getId())) {
            jobExecutorType = JobExecutorType.Heavy;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Sync_fme.getId())) {
            jobExecutorType = JobExecutorType.Sync_fme;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Async_fme.getId())){
            jobExecutorType = JobExecutorType.Async_fme;
        } else {
            jobExecutorType = JobExecutorType.Unknown;
        }
        WorkerStateRabbitMQResponseMessage response = new WorkerStateRabbitMQResponseMessage.WorkerStateRabbitMQResponseBuilder(containerInfoRetriever.getContainerName(), Constants.WORKER_READY)
                .setJobExecutorType(jobExecutorType).setHeartBeatQueue(RabbitMQConfig.queue).build();
        rabbitMQSender.sendWorkerStatus(response);
        LOGGER.info("Message for initializing JobExecutor status sent on rabbitmq");
    }
}
