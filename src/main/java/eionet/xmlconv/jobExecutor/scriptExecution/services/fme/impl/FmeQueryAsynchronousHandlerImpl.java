package eionet.xmlconv.jobExecutor.scriptExecution.services.fme.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.config.StatusInitializer;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeJobStatus;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeServerCommunicator;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class FmeQueryAsynchronousHandlerImpl implements FmeQueryAsynchronousHandler {

    @Value(("${fme_timeout}"))
    private Integer fmeTimeOutProperty;

    private FmeServerCommunicator fmeServerCommunicator;
    private FmeJobsAsyncService fmeJobsAsyncService;
    private RabbitMQSender rabbitMQSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(FmeQueryAsynchronousHandlerImpl.class);

    @Autowired
    public FmeQueryAsynchronousHandlerImpl(FmeServerCommunicator fmeServerCommunicator, FmeJobsAsyncService fmeJobsAsyncService, RabbitMQSender rabbitMQSender) {
        this.fmeServerCommunicator = fmeServerCommunicator;
        this.fmeJobsAsyncService = fmeJobsAsyncService;
        this.rabbitMQSender = rabbitMQSender;
    }

    @Override
    public synchronized void pollFmeServerForResults(Script script, String folderName) throws DatabaseException, GenericFMEexception, RetryCountForGettingJobResultReachedException, InterruptedException, FmeAuthorizationException, FMEBadRequestException, FmeCommunicationException {
        WorkerJobInfoRabbitMQResponseMessage response = new WorkerJobInfoRabbitMQResponseMessage();
        Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(Integer.parseInt(script.getJobId()));
        if (!fmeJobsAsync.isPresent()) {
            return;
        }
        if (checkResultIfReady(fmeJobsAsync.get(), script, fmeServerCommunicator)) {
            String jobId = script.getJobId();
            fmeServerCommunicator.getResultFiles(jobId, folderName, script.getStrResultFile());
            fmeServerCommunicator.deleteFolder(jobId, folderName);
            fmeJobsAsyncService.deleteById(Integer.parseInt(jobId));
            response.setJobExecutorName(StatusInitializer.containerName);
            response.setErrorExists(false).setScript(script).setJobExecutorStatus(Constants.WORKER_READY).setHeartBeatQueue(RabbitMQConfig.queue)
                    .setJobExecutorType(StatusInitializer.jobExecutorType).setScript(script);
            sendResponseToConverters(jobId, response);
        }
    }

    protected synchronized boolean checkResultIfReady(FmeJobsAsync jobAsyncEntry, Script script, FmeServerCommunicator fmeServerCommunicator) throws RetryCountForGettingJobResultReachedException, FMEBadRequestException, FmeCommunicationException, GenericFMEexception, FmeAuthorizationException, InterruptedException, DatabaseException {
        int count = jobAsyncEntry.getCount();
        String convertersJobId = script.getJobId();
        if (jobAsyncEntry.getTimestamp() != null) {
            //will wait 'fmeTimeOutProperty' before trying to retry the FME call
            Duration duration = Duration.between(jobAsyncEntry.getTimestamp(), Instant.now());
            if (duration.toMillis() < fmeTimeOutProperty) {
                return false;
            }
        }
        while (count < jobAsyncEntry.getRetries()) {
            String logMessage = "Retry " + count + " for polling for status of FME job " + jobAsyncEntry.getFmeJobId();
            if (!Utils.isNullStr(convertersJobId)) {
                logMessage += " Converters job id is " + convertersJobId;
            }
            LOGGER.info(logMessage);
            FmeJobStatus jobStatus = fmeServerCommunicator.getJobStatus(jobAsyncEntry.getFmeJobId().toString(), script);
            switch (jobStatus) {
                case SUBMITTED:
                case PULLED:
                case QUEUED: {
                    if (count + 1 == jobAsyncEntry.getRetries()) {
                        String message = "Failed for last Retry  number: " + count + ". Received status " + jobStatus.toString();
                        if (!Utils.isNullStr(convertersJobId)) {
                            message += " Converters job id is " + convertersJobId;
                        }
                        throw new RetryCountForGettingJobResultReachedException(message);
                    } else {
                        String message = "Fme Request Process is still in progress for  -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource() + " -- Response: " + jobStatus.toString() + "-- #Retry: " + count;
                        if (!Utils.isNullStr(convertersJobId)) {
                            message += " Converters job id is " + convertersJobId;
                        }
                        LOGGER.error(message);
                        jobAsyncEntry.setTimestamp(Instant.now());
                    }
                    jobAsyncEntry.setCount(++count).setProcessing(false);
                    fmeJobsAsyncService.save(jobAsyncEntry);
                    return false;
                }
                case ABORTED:
                case FME_FAILURE: {
                    throw new GenericFMEexception("Received result status FME_FAILURE for job Id #" + script.getFmeJobId());
                }

                case SUCCESS:
                    return true;
            }
        }
        throw new RetryCountForGettingJobResultReachedException("Retry count reached with no result");
    }

    @Override
    public void sendResponseToConverters(String jobId, WorkerJobInfoRabbitMQResponseMessage response) {
        LOGGER.info(String.format("Execution of job %s was completed", jobId));
        // The thread is forced to wait for 'timeoutMilisecs' before sending the message to converters in order for the result of the job to be written properly. Refs #140608
        LOGGER.info("Job with id " + jobId + " is waiting for " + Properties.responseTimeoutMs.toString() + " ms");
        try {
            Thread.sleep(Properties.responseTimeoutMs);
        } catch (InterruptedException e) {
            LOGGER.error("Job with id " + jobId + " failed to wait for " + Properties.responseTimeoutMs.toString() + " ms");
        }
        rabbitMQSender.sendMessage(response);
    }
}
