package eionet.xmlconv.jobExecutor.rabbitmq.config;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
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

    @Value("${rancher.jobExecutor.type}")
    private Integer rancherJobExecutorType;

    private JobExecutorType jobExecutorType = JobExecutorType.Unknown;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusInitializer.class);

    public StatusInitializer(RabbitMQSender rabbitMQSender) {
        this.rabbitMQSender = rabbitMQSender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeWorkerStatusAfterStartup() {
        LOGGER.info(String.format("Container name is %s", Properties.RANCHER_POD_NAME));
        jobExecutorType = GenericHandlerUtils.getJobExecutorType(rancherJobExecutorType);
        WorkerStateRabbitMQResponseMessage response = new WorkerStateRabbitMQResponseMessage
                .WorkerStateRabbitMQResponseBuilder(Properties.RANCHER_POD_NAME, Constants.WORKER_READY)
                .setJobExecutorType(jobExecutorType).setHeartBeatQueue(RabbitMQConfig.queue).build();
        rabbitMQSender.sendWorkerStatus(response);
        LOGGER.info("Message for initializing JobExecutor status sent on rabbitmq");
    }
}
