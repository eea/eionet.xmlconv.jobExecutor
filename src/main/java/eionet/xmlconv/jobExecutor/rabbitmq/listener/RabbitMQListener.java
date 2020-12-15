package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import eionet.xmlconv.jobExecutor.exceptions.RabbitMQException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ScriptExecutionServiceImpl;
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
    public void consumeMessage(Script script) throws RabbitMQException{
        LOGGER.info("Received script with id " + script.getJobId());
        String containerName = containerInfoRetriever.getContainerName();

        String jobReceivedMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_RECEIVED, new String[] {containerName, script.getJobId()});
        rabbitMQSender.sendMessage(jobReceivedMessage);

        ses.setScript(script);
        String replyMessage = null;
        StopWatch timer = new StopWatch();
        timer.start();
        try{
            String result = ses.getResult();
            timer.stop();
            replyMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {containerName, script.getJobId(), timer.toString()});
        }
        catch(ScriptExecutionException e){
            timer.stop();
            replyMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {containerName, script.getJobId(), timer.toString()}) + " Error message: " + e.getMessage();
        }
        finally {
            rabbitMQSender.sendMessage(replyMessage);
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));

    }
}
