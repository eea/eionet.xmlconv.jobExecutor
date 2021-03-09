package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.JobExecutionStatus;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkersRabbitMQResponse;
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
import org.springframework.stereotype.Component;


@Component
public class RabbitMQListener {

    private ScriptExecutionService scriptExecutionService;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private DataRetrieverService dataRetrieverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);

    @Autowired
    public RabbitMQListener(ScriptExecutionService scriptExecutionService, RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever,
                            DataRetrieverService dataRetrieverService) {
        this.scriptExecutionService = scriptExecutionService;
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
        this.dataRetrieverService = dataRetrieverService;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(Script script) throws ConvertersCommunicationException {
        LOGGER.info("Received script with id " + script.getJobId());

        JobExecutionStatus jobExecutionStatus = dataRetrieverService.getJobStatus(script.getJobId());
        if (jobExecutionStatus.getStatusId()==Constants.JOB_CANCELLED_BY_USER) {
            throw new AmqpRejectAndDontRequeueException("Job with id " + script.getJobId() + " and status cancelled_by_user was rejected");
        }

        String containerName = containerInfoRetriever.getContainerName();
        LOGGER.info(String.format("Container name is %s", containerName));
        WorkersRabbitMQResponse response = new WorkersRabbitMQResponse().setErrorExists(false)
                .setScript(script).setJobStatus(Constants.WORKER_RECEIVED).setContainerName(containerName);
        rabbitMQSender.sendMessage(response);

        scriptExecutionService.setScript(script);
        StopWatch timer = new StopWatch();
        timer.start();
        try{
            scriptExecutionService.getResult();
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setJobStatus(Constants.WORKER_READY);
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobStatus(Constants.WORKER_READY);
        }
        finally {
            rabbitMQSender.sendMessage(response);
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));

    }
}




