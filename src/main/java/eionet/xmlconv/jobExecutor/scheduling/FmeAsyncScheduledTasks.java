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
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import eionet.xmlconv.jobExecutor.utils.GenericHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Optional;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(FmeAsyncScheduledTasks.class);

    /**
     * Time in milliseconds. Runs 2 minutes
     */
    @Scheduled(fixedRate = 120000)
    public void scheduleFmeAsyncJobsStatusPolling() {
        List<FmeJobsAsync> asyncFmeJobs = fmeJobsAsyncService.findAll();
        asyncFmeJobs.forEach(fmeJobsAsync -> {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Script script;
            try {
                script = mapper.readValue(fmeJobsAsync.getScript(), Script.class);
                Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
                WorkerJobRabbitMQRequestMessage rabbitMQRequest = new WorkerJobRabbitMQRequestMessage().setScript(script);
                if (jobExecutionStatus == Constants.JOB_CANCELLED_BY_USER) {
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job cancelled by user",
                            Constants.JOB_CANCELLED_BY_USER, StatusInitializer.containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else if (jobExecutionStatus == Constants.JOB_INTERRUPTED) {
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was interrupted because duration exceeded schema's maxExecutionTime",
                            Constants.JOB_INTERRUPTED, StatusInitializer.containerName, Constants.WORKER_READY);
                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                }
                else if(jobExecutionStatus == Constants.JOB_DELETED){
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job was deleted",
                            Constants.JOB_DELETED, StatusInitializer.containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else if(jobExecutionStatus == Constants.JOB_FATAL_ERROR || jobExecutionStatus == Constants.JOB_READY){
                    rabbitMQRequest = GenericHandlerUtils.createMessageForDeadLetterQueue(rabbitMQRequest, "Job has already been executed",
                            Constants.JOB_READY, StatusInitializer.containerName, Constants.WORKER_READY);

                    rabbitMQSender.sendMessageToDeadLetterQueue(rabbitMQRequest);
                    deleteEntryFromJobsAsyncTable(fmeJobsAsync.getId());
                } else {
                    fmeQueryAsynchronousHandler.pollFmeServerForResults(script, fmeJobsAsync.getFolderName());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("Error during deserialization of script for job " + fmeJobsAsync.getId());
            } catch (Exception e) {
                LOGGER.error("Error while processing job " + fmeJobsAsync.getId());
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
