package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessageInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import net.xqj.basex.bin.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HeartBeatMessageListener implements MessageListener {

    @Autowired
    ContainerInfoRetriever containerInfoRetriever;
    @Autowired
    private RabbitMQSender rabbitMQSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatMessageListener.class);

    @Override
    public void onMessage(Message message) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WorkerHeartBeatMessageInfo response = null;
        try {
            response = mapper.readValue(message.getBody(), WorkerHeartBeatMessageInfo.class);
            LOGGER.info("Received heart beat message for job " + response.getJobId());
        } catch (IOException e) {
            LOGGER.error("Error during processing of heart beat message, " + e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
        String containerName = containerInfoRetriever.getContainerName();
        Integer jobStatus = ScriptMessageListener.getWorkerJobStatus().get(response.getJobId().toString());
        if (!response.getJobExecutorName().equals(containerName)) {
            throw new AmqpRejectAndDontRequeueException("Worker " + response.getJobExecutorName() + " should receive heart beat message for job " + response.getJobId());
        } else if (jobStatus == null) {
            response.setJobStatus(Constants.JOB_NOT_FOUND);
            sendHeartBeatResponse(response);
        } else if (jobStatus != null) {
            response.setJobStatus(jobStatus);
            sendHeartBeatResponse(response);
        }
    }

    protected void sendHeartBeatResponse(WorkerHeartBeatMessageInfo jobExecInfo) {
        rabbitMQSender.sendHeartBeatMessageResponse(jobExecInfo);
        LOGGER.info("Response for heart beat message of job " + jobExecInfo.getJobId() + " sent");
    }
}
