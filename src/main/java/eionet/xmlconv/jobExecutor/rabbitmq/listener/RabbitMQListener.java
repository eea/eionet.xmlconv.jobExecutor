package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.JobExecutionStatus;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobExecutionInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequest;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;


@Component
public class RabbitMQListener {

    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private DataRetrieverService dataRetrieverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);
    private static volatile Map<String, List<Integer>> workerJob = new HashMap<>();

    @Autowired
    public RabbitMQListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever,
                            DataRetrieverService dataRetrieverService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
        this.dataRetrieverService = dataRetrieverService;
    }

    @RabbitListener(queues = "${jobExec.request.rabbitmq.listeningQueue}")
    public void consumeMsg(@Header(DELIVERY_TAG) long deliveryTag, WorkerJobExecutionInfo jobExecInfo, Channel channel) throws IOException {
        String containerName = containerInfoRetriever.getContainerName();
        if (!jobExecInfo.getJobExecutorName().equals(containerName)) {
            channel.basicReject(deliveryTag, true);
        } else if (workerJob.size()==0) {
            jobExecInfo.setExecuting(false);
            sendMessageForJobExecution(deliveryTag, jobExecInfo, channel);
        } else if (workerJob.get(containerName).contains(jobExecInfo.getJobId())) {
            jobExecInfo.setExecuting(true);
            sendMessageForJobExecution(deliveryTag, jobExecInfo, channel);
        }
    }

    private void sendMessageForJobExecution(long deliveryTag, WorkerJobExecutionInfo jobExecInfo, Channel channel) throws IOException {
        rabbitMQSender.sendMessageForJobExecution(jobExecInfo);
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
            throw new AmqpRejectAndDontRequeueException("Job with id " + script.getJobId() + " and status cancelled_by_user was rejected");
        } else if (rabbitMQRequest.getJobExecutorName()!=null && !rabbitMQRequest.getJobExecutorName().equals(containerName)) {
            channel.basicReject(deliveryTag, true);
        }

        List<Integer> jobList = new ArrayList<>();
        jobList.add(Integer.parseInt(script.getJobId()));
        workerJob.put(containerName, jobList);
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
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobExecutorStatus(Constants.WORKER_READY);
        }
        finally {
            rabbitMQSender.sendMessage(response);
            workerJob.clear();
            channel.basicAck(deliveryTag, true);
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));
    }
}




