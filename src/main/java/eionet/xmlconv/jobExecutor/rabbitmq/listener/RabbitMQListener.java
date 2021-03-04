package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkersRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.apache.commons.lang.time.StopWatch;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;

import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;


@Component
public class RabbitMQListener {

    private ScriptExecutionService ses;
    private RabbitMQSender rabbitMQSender;
    private ContainerInfoRetriever containerInfoRetriever;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);
    private static volatile boolean lock = false;

    @Autowired
    public RabbitMQListener(ScriptExecutionService ses, RabbitMQSender rabbitMQSender, ContainerInfoRetriever containerInfoRetriever) {
        this.ses = ses;
        this.rabbitMQSender = rabbitMQSender;
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(@Header(DELIVERY_TAG) long deliveryTag, Script script, Channel channel) throws Exception {
        if (lock) {
            channel.basicReject(deliveryTag, true);
            return;
        }
        lock = true;
        LOGGER.info("Received script with id " + script.getJobId());

        String containerName = containerInfoRetriever.getContainerName();
        WorkersRabbitMQResponse response = new WorkersRabbitMQResponse().setErrorExists(false)
                .setScript(script).setJobStatus(Constants.WORKER_RECEIVED).setContainerName(containerName);

        rabbitMQSender.sendMessage(response);
        StopWatch timer = new StopWatch();
        executeScript(response, script, containerName, timer);
        rabbitMQSender.sendMessage(response);
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));
        channel.basicAck(deliveryTag, false);
        lock = false;
    }

    @RabbitListener(queues = "${job.rabbitmq.onDemand.listeningQueue}")
    public void consumeOnDemandMessage(@Header(DELIVERY_TAG) long deliveryTag, Script script, Channel channel) throws Exception {
        if (lock) {
            channel.basicReject(deliveryTag, true);
            return;
        }
        lock = true;
        LOGGER.info("Received script with id " + script.getJobId());

        String containerName = containerInfoRetriever.getContainerName();
        WorkersRabbitMQResponse response = new WorkersRabbitMQResponse().setErrorExists(false)
                .setScript(script).setJobStatus(Constants.WORKER_RECEIVED).setContainerName(containerName);

        rabbitMQSender.sendOnDemandMessage(response);
        StopWatch timer = new StopWatch();
        executeScript(response, script, containerName, timer);
        rabbitMQSender.sendOnDemandMessage(response);
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));
        channel.basicAck(deliveryTag, false);
        lock = false;
    }

    synchronized WorkersRabbitMQResponse executeScript(WorkersRabbitMQResponse response, Script script, String containerName, StopWatch timer) {
        ses.setScript(script);
        timer.start();
        try{
            ses.getResult();
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setJobStatus(Constants.WORKER_READY);
        }
        catch(ScriptExecutionException e){
            timer.stop();
            LOGGER.info(Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}));
            response.setErrorExists(true).setErrorMessage(e.getMessage()).setJobStatus(Constants.WORKER_READY);
        }
        return response;
    }
}




