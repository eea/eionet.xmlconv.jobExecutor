package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;

@ConditionalOnProperty(
        value="rabbitmq.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Service
public class HeartBeatMessageListener implements MessageListener {

    @Value("${rancher.heavy.service.name}")
    private String rancherHeavyServiceName;

    @Autowired
    private ContainerInfoRetriever containerInfoRetriever;
    @Autowired
    private RabbitMQSender rabbitMQSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatMessageListener.class);

    @Override
    public void onMessage(Message message) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WorkerHeartBeatMessage response = null;
        try {
            response = mapper.readValue(message.getBody(), WorkerHeartBeatMessage.class);
            LOGGER.info("Received heart beat message for job " + response.getJobId());
        } catch (IOException e) {
            LOGGER.error("Error during processing of heart beat message, " + e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
        ContainerInfo containerInfo = containerInfoRetriever.getContainerInfo();
        response.setJobExecutorType(containerInfo.getService_name().equals(rancherHeavyServiceName) ? JobExecutorType.Heavy : JobExecutorType.Light);
        Integer jobStatus = ScriptMessageListener.getWorkerJobStatus().get(response.getJobId().toString());
        if (!response.getJobExecutorName().equals(containerInfo.getName())) {
            throw new AmqpRejectAndDontRequeueException("Worker " + response.getJobExecutorName() + " should receive heart beat message for job " + response.getJobId());
        } else if (jobStatus == null) {
            response.setJobStatus(Constants.JOB_NOT_FOUND);
            sendHeartBeatResponse(response);
        } else if (jobStatus != null) {
            response.setJobStatus(jobStatus);
            sendHeartBeatResponse(response);
        }
    }

    protected void sendHeartBeatResponse(WorkerHeartBeatMessage jobExecInfo) {
        rabbitMQSender.sendHeartBeatMessageResponse(jobExecInfo);
        LOGGER.info("Response for heart beat message of job " + jobExecInfo.getJobId() + " sent");
    }
}
