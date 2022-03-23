package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.services.FmeJobsAsyncService;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.rabbitmq.service.RabbitMQSender;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FMEUtils;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeExceptionHandlerService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeQueryAsynchronousHandler;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeServerCommunicator;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SynchronousSubmitJobRequest;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service("fmeEngineService")
public class FMEQueryEngineServiceImpl extends ScriptEngineServiceImpl{

    private Environment env;

    private static final Logger LOGGER = LoggerFactory.getLogger(FMEQueryEngineServiceImpl.class);

    private CloseableHttpClient client_ = null;

    private static RequestConfig.Builder requestConfigBuilder = null;

    private String fmeUrl = null;

    private Integer retries = 0;

    private String randomStr;

    private static final String FME_HOST_TEMPORARY_HARDCODED = "fme.discomap.eea.europa.eu";

    private static final String FME_PORT_TEMPORARY_HARDCODED = "443";

    private RabbitMQSender rabbitMQSender;
    private FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler;
    @Autowired(required = false)
    private FmeJobsAsyncService fmeJobsAsyncService;
    private FmeExceptionHandlerService fmeExceptionHandlerService;

    /* Variables for eionet.gdem.Properties*/
    private Integer fmeTimeoutProperty;
    private Integer fmeSocketTimeoutProperty;
    private String fmeHostProperty ;
    private String fmePortProperty ;
    private String fmeTokenExpirationProperty ;
    private String fmeTokenTimeunitProperty;
    private String fmePollingUrlProperty;
    private Integer fmeRetryHoursProperty;
    private String fmeTokenProperty;
    private String fmeSynchronousTokenProperty;


    private String fmeUser ;
    private String fmePassword ;

    private static final String TEST_PROFILE = "test";

    /**
     * Default constructor.
     * @throws Exception If an error occurs.
     */
    @Autowired
    public FMEQueryEngineServiceImpl(Environment env, RabbitMQSender rabbitMQSender, FmeQueryAsynchronousHandler fmeQueryAsynchronousHandler,
                                     FmeExceptionHandlerService fmeExceptionHandlerService) throws Exception {
        this.env = env;

        this.fmeUser = this.env.getProperty("fme_user");
        this.fmeHostProperty = this.env.getProperty("fme.host");
        this.fmePortProperty = this.env.getProperty("fme.port");
        this.fmePassword = this.env.getProperty("fme_user_password");
        this.fmeTokenExpirationProperty = this.env.getProperty("fme_token_expiration");
        this.fmeTokenTimeunitProperty = this.env.getProperty("fme_token_timeunit");
        this.fmeTokenProperty = this.env.getProperty("fme_token");
        this.fmeSynchronousTokenProperty = this.env.getProperty("fme_synchronous_token");
        this.fmeTimeoutProperty = Integer.valueOf(this.env.getProperty("fme_timeout"));
        this.fmeSocketTimeoutProperty = Integer.valueOf(this.env.getProperty("fme_socket_timeout"));
        this.fmePollingUrlProperty = this.env.getProperty("fme_polling_url");
        this.fmeRetryHoursProperty = Integer.valueOf(this.env.getProperty("fme_retry_hours"));

        client_ = HttpClients.createDefault();

        requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(this.getFmeSocketTimeoutProperty());

        this.rabbitMQSender = rabbitMQSender;
        this.fmeQueryAsynchronousHandler = fmeQueryAsynchronousHandler;
        this.fmeExceptionHandlerService = fmeExceptionHandlerService;
    }

    @Override
    protected void runQuery(Script script, OutputStream result, WorkerJobInfoRabbitMQResponseMessage response) throws Exception {

        if(script.getAsynchronousExecution()){
            LOGGER.info("For job " + script.getJobId() + " the script " + script.getScriptFileName() + " will be run asynchronously");
            runQueryAsynchronous(script, result, response);
        }
        else{
            LOGGER.info("For job " + script.getJobId() + " the script " + script.getScriptFileName() + " will be run synchronously");
            runQuerySynchronous(script, result);
        }
    }

    protected void runQuerySynchronous(Script script, OutputStream result) throws GenericFMEexception {

        //fme connection should be skipped in case of tests execution
        String synchronousToken = null;
        boolean skipFMEConnectionInfoCheck = false;
        boolean testProfile = Arrays.asList(env.getActiveProfiles()).stream().allMatch(p -> p.equals(TEST_PROFILE));
        if (testProfile) {
            skipFMEConnectionInfoCheck = true;
        }
        try {
            if (!skipFMEConnectionInfoCheck) {
                //retrieve synchronous token
                synchronousToken = getConnectionInfo(script.getJobId());
            }
        } catch (Exception e) {
            throw new GenericFMEexception(e.toString());
        }
        if(Utils.isNullStr(synchronousToken)){
            throw new GenericFMEexception("Synchronous token is empty");
        }

        HttpPost runMethod = null;
        CloseableHttpResponse response = null;
        int count = 0;
        int retryMilisecs = Properties.fmeRetryHours * 60 * 60 * 1000;
        int timeoutMilisecs = Properties.fmeTimeout;
        int retries = retryMilisecs / timeoutMilisecs;
        retries = (retries <= 0) ? 1 : retries;
        String jobId = script.getJobId();
        while (count < retries) {
            try {
                java.net.URI uri = new URIBuilder(script.getScriptSource())
                        .addParameter("token", synchronousToken)
                        .addParameter("opt_showresult", "true")
                        .addParameter("opt_servicemode", "sync")
                        .addParameter("source_xml", script.getOrigFileUrl()) // XML file
                        .addParameter("format", script.getOutputType())
                        .build(); // Output format

                LOGGER.info("For jobId " + script.getJobId() + " a post request will be made to FME in order to synchronously submit a job with URL " + uri.toURL().toString());
                runMethod = new HttpPost(uri);

                // Request Config (Timeout)
                runMethod.setConfig(requestConfigBuilder.build());
                response = client_.execute(runMethod);
                if (response.getStatusLine().getStatusCode() == 200) { // Valid Result: 200 HTTP status code
                    HttpEntity entity = response.getEntity();
                    // We get an InputStream and copy it to the 'result' OutputStream
                    String logMessage = FMEQueryEngineServiceImpl.class.getName() + ": Synchronous job exeuction ";
                    if (!Utils.isNullStr(jobId)){
                        logMessage += " for job id " + jobId;
                    }
                    logMessage += " got response 200 OK From FME SERVER in :"+ count +" retry . Response is: " + response.toString();
                    LOGGER.info(logMessage);
                    IOUtils.copy(entity.getContent(), result);
                } else { // NOT Valid Result
                    // If the last retry fails a BLOCKER predefined error is returned
                    if (count + 1 == retries){
                        String logMessage = FMEQueryEngineServiceImpl.class.getName() + ": Synchronous job exeuction ";
                        if (!Utils.isNullStr(jobId)){
                            logMessage += " for job id " + jobId;
                        }
                        logMessage += " failed with response code " + response.getStatusLine().getStatusCode() + " for last Retry  number :"+ count + ". Response is: " + response.toString();
                        LOGGER.error(logMessage);

                        IOUtils.copy(IOUtils.toInputStream("<div class=\"feedbacktext\"><span id=\"feedbackStatus\" class=\"BLOCKER\" style=\"display:none\">The QC Process failed, please allow some time and re-run the process. If the issue persists please contact the dataflow helpdesk.</span>The QC Process failed, please allow some time and re-run the process. Please try again. If the issue persists please contact the dataflow helpdesk.</div>", "UTF-8"), result);
                    } else {

                        String logMessage = FMEQueryEngineServiceImpl.class.getName() + ": Synchronous job exeuction ";
                        if (!Utils.isNullStr(jobId)){
                            logMessage += " for job id " + jobId;
                        }
                        logMessage += " failed with response code " + response.getStatusLine().getStatusCode() + ". The application has encountered an error. The FME QC process request failed. -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource() + " -- Response: " + response.toString() + "-- #Retry: " + count;
                        LOGGER.error(logMessage);
                        Thread.sleep(timeoutMilisecs); // The thread is forced to wait 'timeoutMilisecs' before trying to retry the FME call
                        throw new Exception("The application has encountered an error. The FME QC process request failed.");
                    }
                }
                count = retries;
            } catch (SocketTimeoutException e) { // Timeout Exceeded
                FMEUtils.handleSynchronousLastRetryExceptionFailure(retries, count +1, jobId, "The FME request has exceeded the allotted timeout of :"+Properties.fmeSocketTimeout+" -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource(), "SocketTimeoutException", result);
            } catch (Exception e) {
                FMEUtils.handleSynchronousLastRetryExceptionFailure(retries, count +1, jobId, "Generic Exception handling. FME request error: " + e.getMessage(), "Exception", result);
            } finally {
                if (runMethod != null) {
                    runMethod.releaseConnection();
                }
                count++;
            }
        }

    }

    protected void runQueryAsynchronous(Script script, OutputStream result, WorkerJobInfoRabbitMQResponseMessage response) throws IOException, DatabaseException {
        String folderName = FMEUtils.constructFMEFolderName(script.getOrigFileUrl(), this.getRandomStr());
        LOGGER.info("For job id " + script.getJobId() + " the folder we will create in FME server to get the asynchronous results is: " + folderName);
        String fmeJobId="";
        try {

            FmeServerCommunicator fmeServerCommunicator = this.getFmeServerCommunicator();
            fmeJobId = fmeServerCommunicator.submitJob(script,new SynchronousSubmitJobRequest(script.getOrigFileUrl(),folderName));
            sendFMEJobIdToConverters(fmeJobId, response);
            Optional<FmeJobsAsync> fmeJobsAsync = fmeJobsAsyncService.findById(Integer.parseInt(script.getJobId()));
            if (!fmeJobsAsync.isPresent()) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                int retryMilisecs = this.getFmeRetryHoursProperty() * 60 * 60 * 1000;
                int timeoutMilisecs = this.getFmeTimeoutProperty();
                Integer retries = retryMilisecs / timeoutMilisecs;
                FmeJobsAsync asyncEntry = new FmeJobsAsync(Integer.parseInt(script.getJobId())).setFmeJobId(fmeJobId!=null ? Integer.parseInt(fmeJobId) : null).setProcessing(true)
                        .setScript(mapper.writeValueAsString(script)).setRetries(retries <= 0 ? 1 : retries).setCount(0).setFmeJobId(Integer.parseInt(fmeJobId)).setFolderName(folderName);
                fmeJobsAsyncService.save(asyncEntry);
            } else {
                fmeJobsAsync.get().setFmeJobId(Integer.parseInt(fmeJobId)).setFolderName(folderName);
                fmeJobsAsyncService.save(fmeJobsAsync.get());
            }
            fmeQueryAsynchronousHandler.pollFmeServerForResults(script, folderName);
        } catch (FmeAuthorizationException | FmeCommunicationException | DatabaseException | GenericFMEexception | RetryCountForGettingJobResultReachedException | InterruptedException | FMEBadRequestException e) {
            fmeExceptionHandlerService.execute(script, fmeJobId, e.getMessage());
        }
        finally {
            if(!Utils.isNullStr(fmeJobId)){
                script.setFmeJobId(fmeJobId);
            }

        }
    }

    /**
     * Gets a user token from the FME server.
     *
     * @throws Exception If an error occurs.
     */
    private String getConnectionInfo(String jobId) throws Exception {

        if(!Utils.isNullStr(this.fmeSynchronousTokenProperty)){
            LOGGER.info("For job "+ jobId + " a semi-permanent synchronous token for FME will be used");
            return this.fmeSynchronousTokenProperty;
        }
        String synchronousToken = null;

        LOGGER.info("For job "+ jobId + " no semi-permanent synchronous token for FME was found. A new one will be generated");

        HttpPost method = null;
        CloseableHttpResponse response = null;

        try {
            // We must first generate a security token for authentication
            // purposes
            fmeUrl = "https://" + this.getFmeHostProperty()+ ":" + this.getFmePortProperty()
                    + "/fmetoken/generate";

            java.net.URI uri = new URIBuilder(fmeUrl)
                    .addParameter("user", this.fmeUser)
                    .addParameter("password", this.fmePassword)
                    .addParameter("expiration", this.getFmeTokenExpirationProperty())
                    .addParameter("timeunit", this.getFmeTokenTimeunitProperty()).build();
            LOGGER.info("For job " + jobId + " a new synchronous token will be generated from FME. Url: " + fmeUrl + " expiration: " + this.getFmeTokenExpirationProperty() + " timeunit: " + this.getFmeTokenTimeunitProperty());
            method = new HttpPost(uri);
            response = client_.execute(method);
            if (response.getStatusLine().getStatusCode() == 200) {
                LOGGER.info("For job " + jobId + " FME authentication SUCCESS. Response: " + response.toString());

                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                synchronousToken = new String(IOUtils.toByteArray(stream), StandardCharsets.UTF_8);
                IOUtils.closeQuietly(stream);
                return synchronousToken;
            } else {
                LOGGER.error("For job " + jobId + " FME authentication failed. Could not retrieve a Token. Response: " + response.toString());
                throw new GenericFMEexception("FME authentication failed");
            }
        } catch (Exception e) {
            throw new GenericFMEexception(e.toString());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    private void sendFMEJobIdToConverters(String fmeJobId, WorkerJobInfoRabbitMQResponseMessage response){
        response.getScript().setFmeJobId(fmeJobId);
        response.setJobExecutorStatus(Constants.WORKER_RECEIVED_FME_JOB_ID);
        rabbitMQSender.sendMessage(response);
    }


    protected CloseableHttpClient getClient_() {
        return client_;
    }

    protected static RequestConfig.Builder getRequestConfigBuilder() {
        return requestConfigBuilder;
    }

    protected Integer getFmeTimeoutProperty() {
        return fmeTimeoutProperty;
    }

    public Integer getFmeSocketTimeoutProperty() {
        return fmeSocketTimeoutProperty;
    }

    protected String getFmeHostProperty() {
        return fmeHostProperty;
    }

    protected String getFmePortProperty() {
        return fmePortProperty;
    }

    protected String getFmeTokenProperty() {
        return fmeTokenProperty;
    }

    protected String getFmeTokenExpirationProperty() {
        return fmeTokenExpirationProperty;
    }

    protected String getFmeTokenTimeunitProperty() {
        return fmeTokenTimeunitProperty;
    }

    protected String getFmePollingUrlProperty() {
        return fmePollingUrlProperty;
    }

    protected Integer getFmeRetryHoursProperty() {
        return fmeRetryHoursProperty;
    }


    protected Integer getRetries() {
        return retries;
    }

    private void setRetries(Integer retries) {
        this.retries = (retries <= 0) ? 1 : retries;
    }

    public String getRandomStr() {
        return RandomStringUtils.randomAlphanumeric(5);
    }

    public FmeServerCommunicator getFmeServerCommunicator(){
        return (FmeServerCommunicator) SpringApplicationContext.getBean(FmeServerCommunicator.class);
    }

}
