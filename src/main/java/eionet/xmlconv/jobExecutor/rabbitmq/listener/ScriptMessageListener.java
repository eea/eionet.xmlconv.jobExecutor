package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(
        value="rabbitmq.enabled",
        havingValue = "true",
        matchIfMissing = true)
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
            Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
            if (jobExecutionStatus == Constants.JOB_CANCELLED_BY_USER) {
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job cancelled by user",
                        Constants.JOB_CANCELLED_BY_USER, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
            } else if (jobExecutionStatus == Constants.JOB_INTERRUPTED) {
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job was interrupted because duration exceeded schema's maxExecutionTime",
                        Constants.JOB_INTERRUPTED, containerName);
                sendMessageToDeadLetterQueue(rabbitMQRequest);
            }
            else if(jobExecutionStatus == Constants.JOB_DELETED){
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job was deleted",
                        Constants.JOB_DELETED, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
            } else if(jobExecutionStatus == Constants.JOB_FATAL_ERROR || jobExecutionStatus == Constants.JOB_READY){
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job has already been executed",
                        Constants.JOB_READY, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
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
            rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, message,
                    Constants.JOB_EXCEPTION_ERROR, containerName);
            sendMessageToDeadLetterQueue(rabbitMQRequest);
        }
    }

    private WorkerJobRabbitMQRequest createMessageForDeadLetterQueue(WorkerJobRabbitMQRequest request,
                                                 String errorMessage, Integer errorStatus, String containerName){
        request.setScript(request.getScript());
        request.setErrorMessage(errorMessage);
        request.setErrorStatus(errorStatus);
        request.setJobExecutionRetries(request.getJobExecutionRetries());
        request.setJobExecutorName(containerName);
        request.setJobExecutorStatus(Constants.WORKER_FAILED);
        request.setHeartBeatQueue(RabbitMQConfig.queue);
        return request;
    }

    protected void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponse response, StopWatch timer) {
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", jobId, timer.toString()));
        // The thread is forced to wait for 'timeoutMilisecs' before sending the message to converters in order for the result of the job to be written properly. Refs #140608
        LOGGER.info("Job with id " + jobId + " is waiting for " + Properties.responseTimeoutMs.toString() + " ms");
        try {
            Thread.sleep(Properties.responseTimeoutMs);
        } catch (InterruptedException e) {
            LOGGER.error("Job with id " + jobId + " failed to wait for " + Properties.responseTimeoutMs.toString() + " ms");
        }
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

    protected void sendMessageToDeadLetterQueue(WorkerJobRabbitMQRequest message) {
        rabbitMQSender.sendMessageToDeadLetterQueue(message);
    }
}




