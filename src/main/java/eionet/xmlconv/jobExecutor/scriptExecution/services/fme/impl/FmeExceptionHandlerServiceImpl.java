package eionet.xmlconv.jobExecutor.scriptExecution.services.fme.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.config.StatusInitializer;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FMEUtils;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeExceptionHandlerService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FmeExceptionHandlerServiceImpl implements FmeExceptionHandlerService {

    private FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler;
    private FmeJobsAsyncService fmeJobsAsyncService;
    private ContainerInfoRetriever containerInfoRetriever;
    private static final Logger LOGGER = LoggerFactory.getLogger(FmeExceptionHandlerServiceImpl.class);

    @Autowired
    public FmeExceptionHandlerServiceImpl(FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler, FmeJobsAsyncService fmeJobsAsyncService, ContainerInfoRetriever containerInfoRetriever) {
        this.fmeQueryAsynchronousHandler = fmeQueryAsynchronousHandler;
        this.fmeJobsAsyncService = fmeJobsAsyncService;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @Override
    public void execute(Script script, String fmeJobId, String exceptionMessage) throws DatabaseException, IOException {
        String message = "Generic Exception handling ";
        String containerName = "";
        if (StatusInitializer.containerName!=null) {
            containerName = StatusInitializer.containerName;
        } else {
            containerName = containerInfoRetriever.getContainerName();
        }
        if (!Utils.isNullStr(script.getJobId())){
            message += " for job id " + script.getJobId();
        }
        message += " FME request error: " + exceptionMessage;
        LOGGER.error(message);
        String resultStr = FMEUtils.createErrorMessage(fmeJobId, script.getScriptSource(), script.getOrigFileUrl(), exceptionMessage);

        FileOutputStream zipFile = new FileOutputStream(script.getStrResultFile());
        ZipOutputStream out = new ZipOutputStream(zipFile);
        ZipEntry entry = new ZipEntry("output.html");
        out.putNextEntry(entry);
        byte[] data = resultStr.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();
        WorkerJobInfoRabbitMQResponseMessage response = new WorkerJobInfoRabbitMQResponseMessage();
        response.setJobExecutorName(containerName);
        response.setErrorExists(true).setScript(script).setJobExecutorStatus(Constants.WORKER_READY).setHeartBeatQueue(RabbitMQConfig.queue)
                .setJobExecutorType(StatusInitializer.jobExecutorType).setScript(script);
        fmeQueryAsynchronousHandler.sendResponseToConverters(script.getJobId(), response);
        Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(Integer.parseInt(script.getJobId()));
        if (fmeJobsAsync.isPresent()) {
            fmeJobsAsyncService.deleteById(Integer.parseInt(script.getJobId()));
        }
    }
}
