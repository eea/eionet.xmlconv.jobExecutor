package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.RabbitMQException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ScriptExecutionServiceImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.InputStream;

@Component
public class RabbitMQListener {

    ScriptExecutionService ses ;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQListener.class);

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(String message) throws RabbitMQException{
        System.out.println("received message: " + message);
        Script script = createTestObject();

        String jobReceivedMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_RECEIVED, new String[] {script.getJobId()});
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(Properties.jobExecutorApplicationUrl + jobReceivedMessage);
        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            throw new RabbitMQException("Could not send message to rabbitMQ");
        }
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
            throw new RabbitMQException("Received status code " + response.getStatusLine().getStatusCode() + " from rabbitMQ");
        }

        ses = new ScriptExecutionServiceImpl(script);
        String replyMessage = null;
        StopWatch timer = new StopWatch();
        timer.start();
        try{
            ses.getResult();
            timer.stop();
            replyMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_SUCCESS, new String[] {script.getJobId(), timer.toString()});
        }
        catch(ScriptExecutionException e){
            timer.stop();
            replyMessage = Properties.getMessage(Constants.WORKER_LOG_JOB_FAILURE, new String[] {script.getJobId(), timer.toString()});
        }
        finally {
            httppost = new HttpPost(Properties.jobExecutorApplicationUrl + replyMessage);
            //Execute and get the response.
            response = null;
            try {
                response = httpclient.execute(httppost);
            } catch (IOException e) {
                throw new RabbitMQException("Could not send message to rabbitMQ");
            }
            entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                throw new RabbitMQException("Received status code " + response.getStatusLine().getStatusCode() + " from rabbitMQ");
            }
        }
        LOGGER.info(String.format("Execution of job %s was completed, total time of execution: %s", script.getJobId(), timer.toString()));

    }

    private Script createTestObject(){
        Script script = new Script();
        script.setResultFile("/home/denia/Documents/EEA/XMLCONV/scripts/jobApplicationExample/resultFile.html");
        script.setScriptSource("");
        script.setOutputType(Script.SCRIPT_RESULTTYPE_HTML);
        script.setScriptType(Script.SCRIPT_LANG_XQUERY3);
        script.setScriptFileName("/home/denia/Documents/EEA/XMLCONV/scripts/jobApplicationExample/aqd-obligations-checks.xquery");
        script.setSrcFileUrl("https://cdrtest.eionet.europa.eu/ro/colwkcutw/envxxyxia/REP_D-RO_ANPM_20170929_C-001.xml");
        script.setJobId("12345");
        script.setSrcFileDownloaded(false);

        String[] params = new String[2];
        params[0] = "param1";
        params[1] = "param2";
        script.setParams(params);

        // script.setSchema();
        return script;
    }
}
