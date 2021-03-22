package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessageInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HeartBeatMessageListener implements ChannelAwareMessageListener {

    @Autowired
    ContainerInfoRetriever containerInfoRetriever;
    @Autowired
    private RabbitMQSender rabbitMQSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatMessageListener.class);

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        ObjectMapper mapper =new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        long deliveryTag = 0;
        try {
            WorkerHeartBeatMessageInfo response = mapper.readValue(message.getBody(), WorkerHeartBeatMessageInfo.class);
            String containerName = containerInfoRetriever.getContainerName();
            Integer jobStatus = ScriptMessageListener.getWorkerJobStatus().get(response.getJobId().toString());
            deliveryTag = message.getMessageProperties().getDeliveryTag();
            if (!response.getJobExecutorName().equals(containerName)) {
                channel.basicReject(deliveryTag, false);
            } else if (jobStatus==null) {
                response.setJobStatus(Constants.JOB_NOT_FOUND);
                sendHeartBeatResponse(deliveryTag, response, channel);
            } else if (jobStatus!=null) {
                response.setJobStatus(jobStatus);
                sendHeartBeatResponse(deliveryTag, response, channel);
            }
        } catch (IOException e) {
            LOGGER.info("Error during processing of heart beat message, " + e.getMessage());
            channel.basicReject(deliveryTag, true);
        }
    }

    protected void sendHeartBeatResponse(long deliveryTag, WorkerHeartBeatMessageInfo jobExecInfo, Channel channel) throws IOException {
        rabbitMQSender.sendHeartBeatMessageResponse(jobExecInfo);
        channel.basicAck(deliveryTag, true);
    }
}
