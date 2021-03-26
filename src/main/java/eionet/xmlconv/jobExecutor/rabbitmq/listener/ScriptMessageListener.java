package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rabbitmq.client.Channel;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.JobExecutionStatus;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
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
public class ScriptMessageListener {

    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private DataRetrieverService dataRetrieverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptMessageListener.class);
    private static volatile Map<String, Integer> workerJobStatus = new HashMap<>();

    @Autowired
    public ScriptMessageListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever,
                                 DataRetrieverService dataRetrieverService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
        this.dataRetrieverService = dataRetrieverService;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(WorkerJobRabbitMQRequest rabbitMQRequest) throws IOException {
        Script script = rabbitMQRequest.getScript();
        LOGGER.info("Received script with id " + script.getJobId());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String scriptStr = ow.writeValueAsString(script);

        WorkerJobInfoRabbitMQResponse response = new WorkerJobInfoRabbitMQResponse();
        StopWatch timer = new StopWatch();
        String containerName = "";
        try{
            containerName = containerInfoRetriever.getContainerName();
            LOGGER.info(String.format("Container name is %s", containerName));
            JobExecutionStatus jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
            if (jobExecutionStatus.getStatusId() == Constants.JOB_CANCELLED_BY_USER) {
                response = createMessageForDeadLetterQueue(rabbitMQRequest,  response, "Job cancelled by user",
                        Constants.JOB_CANCELLED_BY_USER, containerName);

                sendMessageToDeadLetterQueue(response);
            } else {
                clearWorkerJobStatus();
                setWorkerJobStatus(script.getJobId(), Constants.JOB_PROCESSING);
                response.setErrorExists(false).setScript(script).setJobExecutorStatus(Constants.WORKER_RECEIVED).setJobExecutorName(containerName).setHeartBeatQueue(RabbitMQConfig.queue);
                rabbitMQSender.sendMessage(response);

                scriptExecutionService.setScript(script);
                timer.start();
                scriptExecutionService.getResult();
                timer.stop();

                LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[]{containerName, script.getJobId(), timer.toString()}));
                response.setJobExecutorStatus(Constants.WORKER_READY);
                setWorkerJobStatus(script.getJobId(), Constants.JOB_READY);
                sendResponseToConverters(script.getJobId(), response, timer);
            }
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobExecutorStatus(Constants.WORKER_READY);
            setWorkerJobStatus(script.getJobId(), Constants.JOB_FATAL_ERROR);
            sendResponseToConverters(script.getJobId(), response, timer);
        }
        catch (Exception e) {
            String message = e.getMessage();
            if(message == null){
                message = "Unknown error";
            }
            LOGGER.info(message);
            response = createMessageForDeadLetterQueue(rabbitMQRequest,  response, message,
                    Constants.JOB_EXCEPTION_ERROR, containerName);
            sendMessageToDeadLetterQueue(response);
        }
    }

    private WorkerJobInfoRabbitMQResponse createMessageForDeadLetterQueue(WorkerJobRabbitMQRequest request, WorkerJobInfoRabbitMQResponse response,
                                                 String errorMessage, Integer errorStatus, String containerName){
        response.setScript(request.getScript());
        response.setErrorExists(true);
        response.setErrorMessage(errorMessage);
        response.setErrorStatus(errorStatus);
        response.setJobExecutionRetries(request.getJobExecutionRetries());
        response.setJobExecutorName(containerName);
        response.setJobExecutorStatus(Constants.WORKER_FAILED);
        response.setHeartBeatQueue(RabbitMQConfig.queue);
        return response;
    }

    protected void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponse response, StopWatch timer) {
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", jobId, timer.toString()));
        rabbitMQSender.sendMessage(response);
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

    protected void sendMessageToDeadLetterQueue(WorkerJobInfoRabbitMQResponse message) {
        rabbitMQSender.sendMessageToDeadLetterQueue(message);
    }
}




