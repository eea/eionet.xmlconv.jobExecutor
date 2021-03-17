package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.JobExecutionStatus;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerHeartBeatMessageInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequest;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;


@Component
public class RabbitMQListener {

    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private DataRetrieverService dataRetrieverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);
    private static volatile Map<String, Integer> workerJobStatus = new HashMap<>();

    @Autowired
    public RabbitMQListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever,
                            DataRetrieverService dataRetrieverService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
        this.dataRetrieverService = dataRetrieverService;
    }

    @RabbitListener(queues = "${heartBeat.request.rabbitmq.listeningQueue}")
    public void consumeHeartBeatMsgRequest(@Header(DELIVERY_TAG) long deliveryTag, WorkerHeartBeatMessageInfo jobExecInfo, Channel channel) throws IOException {
        String containerName = containerInfoRetriever.getContainerName();
        Integer jobStatus = getWorkerJobStatus().get(jobExecInfo.getJobId().toString());
        if (!jobExecInfo.getJobExecutorName().equals(containerName)) {
            channel.basicReject(deliveryTag, true);
        } else if (jobStatus==null) {
            jobExecInfo.setJobStatus(Constants.JOB_NOT_FOUND);
            sendHeartBeatResponse(deliveryTag, jobExecInfo, channel);
        } else if (jobStatus!=null) {
            jobExecInfo.setJobStatus(jobStatus);
            sendHeartBeatResponse(deliveryTag, jobExecInfo, channel);
        }
    }

    private void sendHeartBeatResponse(long deliveryTag, WorkerHeartBeatMessageInfo jobExecInfo, Channel channel) throws IOException {
        rabbitMQSender.sendHeartBeatMessageResponse(jobExecInfo);
        channel.basicAck(deliveryTag, true);
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(@Header(DELIVERY_TAG) long deliveryTag, WorkerJobRabbitMQRequest rabbitMQRequest, Channel channel) throws ConvertersCommunicationException, IOException {
        Script script = rabbitMQRequest.getScript();
        LOGGER.info("Received script with id " + script.getJobId());

        String containerName = containerInfoRetriever.getContainerName();
        LOGGER.info(String.format("Container name is %s", containerName));

        JobExecutionStatus jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
        if (jobExecutionStatus.getStatusId()==Constants.JOB_CANCELLED_BY_USER) {
            channel.basicReject(deliveryTag, false);
            return;
        }

        clearWorkerJobStatus();
        setWorkerJobStatus(script.getJobId(), Constants.JOB_PROCESSING);
        WorkerJobInfoRabbitMQResponse response = new WorkerJobInfoRabbitMQResponse().setErrorExists(false)
                .setScript(script).setJobExecutorStatus(Constants.WORKER_RECEIVED).setJobExecutorName(containerName);
        rabbitMQSender.sendMessage(response);

        scriptExecutionService.setScript(script);
        StopWatch timer = new StopWatch();
        timer.start();
        try{
            scriptExecutionService.getResult();
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setJobExecutorStatus(Constants.WORKER_READY);
            setWorkerJobStatus(script.getJobId(), Constants.JOB_READY);
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobExecutorStatus(Constants.WORKER_READY);
            setWorkerJobStatus(script.getJobId(), Constants.JOB_FATAL_ERROR);
        }
        finally {
            rabbitMQSender.sendMessage(response);
            channel.basicAck(deliveryTag, true);
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));
    }

    public static synchronized Map<String, Integer> getWorkerJobStatus() {
        return workerJobStatus;
    }

    public static synchronized void setWorkerJobStatus(String jobId, Integer jobStatus) {
        workerJobStatus.put(jobId, jobStatus);
    }

    public static synchronized void clearWorkerJobStatus() {
        workerJobStatus.clear();
    }
}




