package eionet.xmlconv.jobExecutor.scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.StatusInitializer;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeExceptionHandlerService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import eionet.xmlconv.jobExecutor.utils.GenericHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@ConditionalOnProperty(prefix = "enable", name = "fmeScheduler", havingValue = "true")
@Configuration
@EnableScheduling
public class FmeAsyncScheduledTasks {

    @Autowired
    private FmeJobsAsyncService fmeJobsAsyncService;
    @Autowired
    private FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler;
    @Autowired
    private DataRetrieverService dataRetrieverService;
    @Autowired
    private RabbitMQSender rabbitMQSender;
    @Autowired
    private FmeExceptionHandlerService fmeExceptionHandlerService;
    @Autowired
    private ContainerInfoRetriever containerInfoRetriever;

    private static final Logger LOGGER = LoggerFactory.getLogger(FmeAsyncScheduledTasks.class);

    /**
     * Time in milliseconds. Runs every 2 minutes
     * Finds fme asynchronous jobs and check for their status in fme server. If ready result file is update and converters tables are updated through rabbitmq messages.
     * Moreover, fme async entries for ready jobs are deleted from FME_JOBS_ASYNC table.
     */
    @Scheduled(fixedRate = 120000)
    public void scheduleFmeAsyncJobsStatusPolling() {
        LOGGER.info("Running task scheduleFmeAsyncJobsStatusPolling");
        List<FmeJobsAsync> asyncFmeJobs = fmeJobsAsyncService.findAll();
        asyncFmeJobs.forEach(fmeJobsAsync -> {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Script script = null;
            String containerName = "";
            try {
                script = mapper.readValue(fmeJobsAsync.getScript(), Script.class);
                Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
                WorkerJobRabbitMQRequestMessage rabbitMQRequest = new WorkerJobRabbitMQRequestMessage().setScript(script);
                if (StatusInitializer.containerName!=null) {
                    containerName = StatusInitializer.containerName;
                } else {
                    containerName = containerInfoRetriever.getContainerName();
                }
                if (jobExecutionStatus == Constants.JOB_CANCELLED_BY_USER) {
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job cancelled by user",
                            Constants.JOB_CANCELLED_BY_USER, containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else if (jobExecutionStatus == Constants.JOB_INTERRUPTED) {
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was interrupted because duration exceeded schema's maxExecutionTime",
                            Constants.JOB_INTERRUPTED, containerName, Constants.WORKER_READY);
                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                }
                else if(jobExecutionStatus == Constants.JOB_DELETED){
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was deleted",
                            Constants.JOB_DELETED, containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else if(jobExecutionStatus == Constants.JOB_FATAL_ERROR || jobExecutionStatus == Constants.JOB_READY){
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job has already been executed",
                            Constants.JOB_READY, containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else {
                    if (!fmeJobsAsync.isProcessing()) {
                        fmeQueryAsynchronousHandler.pollFmeServerForResults(script, fmeJobsAsync.getFolderName());
                    }
                }
            } catch (JsonProcessingException ex) {
                LOGGER.error("Error while deserializing script of job " + fmeJobsAsync.getId());
            } catch (Exception e) {
                LOGGER.error("Error while processing job " + fmeJobsAsync.getId());
                try {
                    fmeExceptionHandlerService.execute(script, script.getFmeJobId(), e.getMessage());
                } catch (DatabaseException | IOException exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

    protected void deleteEntryFromJobsAsyncTable(Integer jobId) throws DatabaseException {
        Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(jobId);
        if (fmeJobsAsync.isPresent()) {
            fmeJobsAsyncService.deleteById(jobId);
        }
    }
}
