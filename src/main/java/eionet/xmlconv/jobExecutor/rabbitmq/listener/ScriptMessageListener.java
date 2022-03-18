package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.config.StatusInitializer;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConditionalOnProperty(
        value="rabbitmq.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class ScriptMessageListener {

    @Value("${fme_retry_hours}")
    private Integer fmeRetryHoursProperty;
    @Value(("${fme_timeout}"))
    private Integer fmeTimeOutProperty;

    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private DataRetrieverService dataRetrieverService;
    private FmeJobsAsyncService fmeJobsAsyncService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptMessageListener.class);
    private static volatile Map<String, Integer> workerJobStatus = new HashMap<>();
    private static final String FME_SCRIPT_TYPE = "fme";

    @Autowired
    public ScriptMessageListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender,
                                 DataRetrieverService dataRetrieverService, FmeJobsAsyncService fmeJobsAsyncService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.dataRetrieverService = dataRetrieverService;
        this.fmeJobsAsyncService = fmeJobsAsyncService;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(WorkerJobRabbitMQRequestMessage rabbitMQRequest) {
        Script script = rabbitMQRequest.getScript();
        LOGGER.info("Received job with id " + script.getJobId());
        WorkerJobInfoRabbitMQResponseMessage response = new WorkerJobInfoRabbitMQResponseMessage();
        StopWatch timer = new StopWatch();
        String containerName = StatusInitializer.containerName;
        try{
            LOGGER.info(String.format("For job id " + script.getJobId() + " container name is %s", containerName));
            LOGGER.info(String.format("Container name is %s", containerName));
            Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
            if (jobExecutionStatus == Constants.JOB_CANCELLED_BY_USER) {
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job cancelled by user",
                        Constants.JOB_CANCELLED_BY_USER, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                deleteEntryFromJobsAsyncTable(script);
            } else if (jobExecutionStatus == Constants.JOB_INTERRUPTED) {
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job was interrupted because duration exceeded schema's maxExecutionTime",
                        Constants.JOB_INTERRUPTED, containerName);
                sendMessageToDeadLetterQueue(rabbitMQRequest);
                deleteEntryFromJobsAsyncTable(script);
            }
            else if(jobExecutionStatus == Constants.JOB_DELETED){
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job was deleted",
                        Constants.JOB_DELETED, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                deleteEntryFromJobsAsyncTable(script);
            } else if(jobExecutionStatus == Constants.JOB_FATAL_ERROR || jobExecutionStatus == Constants.JOB_READY){
                rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, "Job has already been executed",
                        Constants.JOB_READY, containerName);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                deleteEntryFromJobsAsyncTable(script);
            } else {
                clearWorkerJobStatus();
                if (script.getScriptType().equals(FME_SCRIPT_TYPE)) {
                    if (script.getAsynchronousExecution()) {
                        int retryMilisecs = fmeRetryHoursProperty * 60 * 60 * 1000;
                        int timeoutMilisecs = fmeTimeOutProperty;
                        Integer retries = retryMilisecs / timeoutMilisecs;
                        String fmeJobId = script.getFmeJobId();
                        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        FmeJobsAsync fmeJobsAsync = new FmeJobsAsync(Integer.parseInt(script.getJobId())).setFmeJobId(fmeJobId!=null ? Integer.parseInt(fmeJobId) : null)
                                .setScript(mapper.writeValueAsString(script)).setRetries(retries <= 0 ? 1 : retries).setCount(0);
                        fmeJobsAsyncService.save(fmeJobsAsync);
                    }
                } else {
                    setWorkerJobStatus(script.getJobId(), Constants.JOB_PROCESSING);
                }
                response.setJobExecutorName(containerName);
                response.setErrorExists(false).setScript(script).setJobExecutorStatus(Constants.WORKER_RECEIVED).setHeartBeatQueue(RabbitMQConfig.queue)
                    .setJobExecutorType(StatusInitializer.jobExecutorType);
                rabbitMQSender.sendMessage(response);

                scriptExecutionService.setScript(script);
                timer.start();
                scriptExecutionService.getResult(response);
                timer.stop();

                if (script.getScriptType().equals(FME_SCRIPT_TYPE)) {
                    if (script.getAsynchronousExecution()) {
                        WorkerStateRabbitMQResponseMessage workerStatus = new WorkerStateRabbitMQResponseMessage.WorkerStateRabbitMQResponseBuilder(containerName, Constants.WORKER_READY)
                                .setJobExecutorType(StatusInitializer.jobExecutorType).setHeartBeatQueue(RabbitMQConfig.queue).build();
                        rabbitMQSender.sendWorkerStatus(workerStatus);
                        return;
                    }
                }

                LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[]{containerName, script.getJobId(), timer.toString()}));
                response.setJobExecutorStatus(Constants.WORKER_READY);
                response.setScript(script);
                setWorkerJobStatus(script.getJobId(), Constants.JOB_READY);
                sendResponseToConverters(script.getJobId(), response, timer);
            }
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobExecutorStatus(Constants.WORKER_READY)
                    .setJobExecutorType(StatusInitializer.jobExecutorType);
            setWorkerJobStatus(script.getJobId(), Constants.JOB_FATAL_ERROR);
            sendResponseToConverters(script.getJobId(), response, timer);
        }
        catch (Exception e) {
            String message = e.getMessage();
            if(message == null){
                message = "Unknown error";
            }
            message += " Job id is " + script.getJobId();
            LOGGER.info(message);
            rabbitMQRequest = createMessageForDeadLetterQueue(rabbitMQRequest, message,
                    Constants.JOB_EXCEPTION_ERROR, containerName);
            sendMessageToDeadLetterQueue(rabbitMQRequest);
        }
    }

    private void deleteEntryFromJobsAsyncTable(Script script) {
        Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(Integer.parseInt(script.getJobId()));
        if (fmeJobsAsync.isPresent()) {
            fmeJobsAsyncService.deleteById(Integer.parseInt(script.getJobId()));
        }
    }

    private WorkerJobRabbitMQRequestMessage createMessageForDeadLetterQueue(WorkerJobRabbitMQRequestMessage request,
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

    protected void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponseMessage response, StopWatch timer) {
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

    protected void sendMessageToDeadLetterQueue(WorkerJobRabbitMQRequestMessage message) {
        rabbitMQSender.sendMessageToDeadLetterQueue(message);
    }
}




