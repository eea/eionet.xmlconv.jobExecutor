package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import eionet.xmlconv.jobExecutor.exceptions.RabbitMQException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkersRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang.time.StopWatch;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;


@Component
public class RabbitMQListener {

    @Autowired
    private ScriptExecutionService ses;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);

    @Autowired
    public RabbitMQListener(RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever) {
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(Script script) {
        LOGGER.info("Received script with id " + script.getJobId());

        String containerName = containerInfoRetriever.getContainerName();
        WorkersRabbitMQResponse response = new WorkersRabbitMQResponse().setHasError(false)
                .setXqScript(script).setJobStatus(Constants.XQ_WORKER_RECEIVED).setContainerName(containerName);
        rabbitMQSender.sendMessage(response);

        ses.setScript(script);
        StopWatch timer = new StopWatch();
        timer.start();
        try{
            ses.getResult();
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setJobStatus(Constants.XQ_WORKER_SUCCESS);
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setHasError(true).setErrorMessage(e.getMessage()).setJobStatus(Constants.XQ_WORKER_FATAL_ERR);
        }
        finally {
            rabbitMQSender.sendMessage(response);
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));

    }
}




