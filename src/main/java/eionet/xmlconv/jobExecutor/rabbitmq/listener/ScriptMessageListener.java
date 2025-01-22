package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import eionet.xmlconv.jobExecutor.utils.GenericHandlerUtils;
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

    @Autowired(required = false)
    private FmeJobsAsyncService fmeJobsAsyncService;
    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private DataRetrieverService dataRetrieverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptMessageListener.class);
    private static volatile Map<String, Integer> workerJobStatus = new HashMap<>();
    private static final String FME_SCRIPT_TYPE = "fme";

    @Autowired
    public ScriptMessageListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender,
                                 DataRetrieverService dataRetrieverService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.dataRetrieverService = dataRetrieverService;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(WorkerJobRabbitMQRequestMessage rabbitMQRequest) {
        Script script = rabbitMQRequest.getScript();
        LOGGER.info("Received job with id " + script.getJobId());
        WorkerJobInfoRabbitMQResponseMessage response = new WorkerJobInfoRabbitMQResponseMessage();
        StopWatch timer = new StopWatch();
        JobExecutorType jobExecutorType = JobExecutorType.Unknown;
        try{
            jobExecutorType = GenericHandlerUtils.getJobExecutorType(Properties.rancherJobExecutorType);

            LOGGER.info(String.format("For job id " + script.getJobId() + " pod name is %s", Properties.RANCHER_POD_NAME));
            Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
            LOGGER.info("Job status is " + jobExecutionStatus);
            
            if (jobExecutionStatus == Constants.JOB_CANCELLED_BY_USER) {
                rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job cancelled by user",
                        Constants.JOB_CANCELLED_BY_USER, Properties.RANCHER_POD_NAME, Constants.WORKER_READY);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                if (jobExecutorType!=null && jobExecutorType.equals(JobExecutorType.Async_fme)) {
                    deleteEntryFromJobsAsyncTable(Integer.parseInt(script.getJobId()));
                }
            } else if (jobExecutionStatus == Constants.JOB_INTERRUPTED) {
                rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was interrupted because duration exceeded schema's maxExecutionTime",
                        Constants.JOB_INTERRUPTED, Properties.RANCHER_POD_NAME, Constants.WORKER_READY);
                sendMessageToDeadLetterQueue(rabbitMQRequest);
                if (jobExecutorType!=null && jobExecutorType.equals(JobExecutorType.Async_fme)) {
                    deleteEntryFromJobsAsyncTable(Integer.parseInt(script.getJobId()));
                }
            }
            else if(jobExecutionStatus == Constants.JOB_DELETED){
                rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was deleted",
                        Constants.JOB_DELETED, Properties.RANCHER_POD_NAME, Constants.WORKER_READY);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                if (jobExecutorType!=null && jobExecutorType.equals(JobExecutorType.Async_fme)) {
                    deleteEntryFromJobsAsyncTable(Integer.parseInt(script.getJobId()));
                }
            } else if(jobExecutionStatus == Constants.JOB_FATAL_ERROR || jobExecutionStatus == Constants.JOB_READY){
                rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job has already been executed",
                        Constants.JOB_READY, Properties.RANCHER_POD_NAME, Constants.WORKER_READY);

                sendMessageToDeadLetterQueue(rabbitMQRequest);
                if (jobExecutorType!=null && jobExecutorType.equals(JobExecutorType.Async_fme)) {
                    deleteEntryFromJobsAsyncTable(Integer.parseInt(script.getJobId()));
                }
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
                                .setScript(mapper.writeValueAsString(script)).setRetries(retries <= 0 ? 1 : retries).setCount(0).setProcessing(true);
                        fmeJobsAsyncService.save(fmeJobsAsync);
                    } else {
                        setWorkerJobStatus(script.getJobId(), Constants.JOB_PROCESSING);
                    }
                } else {
                    setWorkerJobStatus(script.getJobId(), Constants.JOB_PROCESSING);
                }
                response.setJobExecutorName(Properties.RANCHER_POD_NAME);
                response.setErrorExists(false).setScript(script).setJobExecutorStatus(Constants.WORKER_RECEIVED).setHeartBeatQueue(RabbitMQConfig.queue)
                    .setJobExecutorType(jobExecutorType);
                rabbitMQSender.sendMessage(response);

                scriptExecutionService.setScript(script);
                timer.start();
                scriptExecutionService.getResult(response);
                timer.stop();

                if (script.getScriptType().equals(FME_SCRIPT_TYPE)) {
                    if (script.getAsynchronousExecution()) {
                        WorkerStateRabbitMQResponseMessage workerStatus = new WorkerStateRabbitMQResponseMessage
                                .WorkerStateRabbitMQResponseBuilder(Properties.RANCHER_POD_NAME, Constants.WORKER_READY)
                                .setJobExecutorType(jobExecutorType).setHeartBeatQueue(RabbitMQConfig.queue).build();
                        rabbitMQSender.sendWorkerStatus(workerStatus);
                        return;
                    }
                }

                LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[]{Properties.RANCHER_POD_NAME, script.getJobId(), timer.toString()}));
                response.setJobExecutorStatus(Constants.WORKER_READY);
                response.setScript(script);
                setWorkerJobStatus(script.getJobId(), Constants.JOB_READY);
                sendResponseToConverters(script.getJobId(), response, timer);
            }
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {Properties.RANCHER_POD_NAME, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobExecutorStatus(Constants.WORKER_READY)
                    .setJobExecutorType(jobExecutorType);
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
            rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, message,
                    Constants.JOB_EXCEPTION_ERROR, Properties.RANCHER_POD_NAME, Constants.WORKER_READY);
            sendMessageToDeadLetterQueue(rabbitMQRequest);
        }
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

    protected void deleteEntryFromJobsAsyncTable(Integer jobId) throws DatabaseException {
        Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(jobId);
        if (fmeJobsAsync.isPresent()) {
            fmeJobsAsyncService.deleteById(jobId);
        }
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




