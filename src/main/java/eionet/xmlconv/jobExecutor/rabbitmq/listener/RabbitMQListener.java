package eionet.xmlconv.jobExecutor.rabbitmq.listener;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptExecutionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ScriptExecutionServiceImpl;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RabbitMQListener {

    ScriptExecutionService ses ;

    @RabbitListener(queues = "${job.rabbitmq.listeningQueue}")
    public void consumeMessage(String message) throws ScriptExecutionException {
        System.out.println("received message: " + message);
        Script script = createTestObject();
        ses = new ScriptExecutionServiceImpl(script);
        ses.getResult();

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
