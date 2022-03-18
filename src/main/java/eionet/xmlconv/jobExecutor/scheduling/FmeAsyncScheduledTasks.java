package eionet.xmlconv.jobExecutor.scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableScheduling
public class FmeAsyncScheduledTasks {

    @Autowired
    private FmeJobsAsyncService fmeJobsAsyncService;
    @Autowired
    private FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler;
    @Autowired
    private DataRetrieverService dataRetrieverService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FmeAsyncScheduledTasks.class);

    /**
     * Time in milliseconds. Runs 2 minutes
     */
    @Scheduled(fixedRate = 120000)
    public void scheduleFixedRateTaskAsync() {
        List<FmeJobsAsync> asyncFmeJobs = fmeJobsAsyncService.findAll();
        asyncFmeJobs.forEach(fmeJobsAsync -> {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Script script;
            try {
                script = mapper.readValue(fmeJobsAsync.getScript(), Script.class);
                Integer jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
                if (jobExecutionStatus != Constants.JOB_PROCESSING) {
                    fmeJobsAsyncService.deleteById(fmeJobsAsync.getId());
                }
                fmeQueryAsynchronousHandler.pollFmeServerForResults(script, fmeJobsAsync.getFolderName());
            } catch (JsonProcessingException e) {
                LOGGER.error("Error during deserialization of script for job " + fmeJobsAsync.getId());
            } catch (IOException | ConvertersCommunicationException | DatabaseException e) {
                LOGGER.error("Error while processing job " + fmeJobsAsync.getId());
            }
        });
    }
}
