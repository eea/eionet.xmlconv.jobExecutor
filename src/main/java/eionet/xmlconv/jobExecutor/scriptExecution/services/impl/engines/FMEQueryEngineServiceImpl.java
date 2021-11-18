package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FMEUtils;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeJobStatus;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeServerCommunicator;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SynchronousSubmitJobRequest;
import eionet.xmlconv.jobExecutor.utils.Utils;
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

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import eionet.xmlconv.jobExecutor.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.apache.commons.io.IOUtils;

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


    /* Variables for eionet.gdem.Properties*/
    private Integer fmeTimeoutProperty;
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
    public FMEQueryEngineServiceImpl(Environment env) throws Exception {
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
        this.fmePollingUrlProperty = this.env.getProperty("fme_polling_url");
        this.fmeRetryHoursProperty = Integer.valueOf(this.env.getProperty("fme_retry_hours"));

        client_ = HttpClients.createDefault();

        requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(this.getFmeTimeoutProperty());
    }

    @Override
    protected void runQuery(Script script, OutputStream result) throws Exception {

        if(script.getAsynchronousExecution()){
            LOGGER.info("For job " + script.getJobId() + " the script " + script.getScriptFileName() + " will be run asynchronously");
            runQueryAsynchronous(script, result);
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
                String logMessage = FMEQueryEngineServiceImpl.class.getName() + ": Synchronous job exeuction ";
                if (!Utils.isNullStr(jobId)){
                    logMessage += " for job id " + jobId;
                }
                logMessage += " failed with SocketTimeoutException. Retries = "+count+"\n The FME request has exceeded the allotted timeout of :"+Properties.fmeTimeout+" -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource();
                LOGGER.error(logMessage);
            } catch (Exception e) {
                LOGGER.error("For job id " + jobId  + " Generic Exception handling. FME request error: " + e.getMessage());
            } finally {
                if (runMethod != null) {
                    runMethod.releaseConnection();
                }
                count++;
            }
        }

    }

    protected void runQueryAsynchronous(Script script, OutputStream result) throws IOException {
        String folderName = FMEUtils.constructFMEFolderName(script.getOrigFileUrl(), this.getRandomStr());
        LOGGER.info("For job id " + script.getJobId() + " the folder we will create in FME server to get the asynchronous results is: " + folderName);
        String jobId="";
        String convertersJobId = script.getJobId();
        try {

            FmeServerCommunicator fmeServerCommunicator = this.getFmeServerCommunicator();
            jobId = fmeServerCommunicator.submitJob(script,new SynchronousSubmitJobRequest(script.getOrigFileUrl(),folderName));


            this.pollFmeServerWithRetries(jobId,script,fmeServerCommunicator);

            fmeServerCommunicator.getResultFiles(script.getJobId(), folderName, script.getStrResultFile());
            fmeServerCommunicator.deleteFolder(script.getJobId(), folderName);
        } catch (FmeAuthorizationException | FmeCommunicationException | GenericFMEexception | FMEBadRequestException |RetryCountForGettingJobResultReachedException | InterruptedException e) {
            String message = "Generic Exception handling ";
            if (!Utils.isNullStr(convertersJobId)){
                message += " for job id " + convertersJobId;
            }
            message += " FME request error: " + e.getMessage();
            LOGGER.error(message);
            String resultStr = FMEUtils.createErrorMessage(jobId, script.getScriptSource(), script.getOrigFileUrl(), e.getMessage());

            FileOutputStream zipFile = new FileOutputStream(script.getStrResultFile());
            ZipOutputStream out = new ZipOutputStream(zipFile);
            ZipEntry entry = new ZipEntry("output.html");
            out.putNextEntry(entry);
            byte[] data = resultStr.getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        }
    }

    protected void pollFmeServerWithRetries(String jobId, Script script,FmeServerCommunicator fmeServerCommunicator) throws RetryCountForGettingJobResultReachedException, FMEBadRequestException, FmeCommunicationException, GenericFMEexception, FmeAuthorizationException, InterruptedException {
        int count = 0;
        int retryMilisecs = this.getFmeRetryHoursProperty() * 60 * 60 * 1000;
        int timeoutMilisecs = this.getFmeTimeoutProperty();
        this.setRetries(retryMilisecs / timeoutMilisecs);
        String convertersJobId = script.getJobId();
        while (count < this.getRetries()) {
            String logMessage = "Retry " + count + " for polling for status of FME job " + jobId;
            if (!Utils.isNullStr(convertersJobId)){
                logMessage += " Converters job id is " + convertersJobId;
            }
            LOGGER.info(logMessage);
            FmeJobStatus jobStatus = fmeServerCommunicator.getJobStatus(jobId,script);
            switch (jobStatus){
                case SUBMITTED:
                case PULLED:
                case QUEUED: {
                    if (count + 1 == this.getRetries()) {
                        String message = "Failed for last Retry  number: " + count + ". Received status " + jobStatus.toString();
                        if (!Utils.isNullStr(convertersJobId)){
                            message += " Converters job id is " + convertersJobId;
                        }
                        throw new RetryCountForGettingJobResultReachedException(message);
                    } else {
                        String message = "Fme Request Process is still in progress for  -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource() + " -- Response: " + jobStatus.toString() + "-- #Retry: " + count;
                        if (!Utils.isNullStr(convertersJobId)){
                            message += " Converters job id is " + convertersJobId;
                        }
                        LOGGER.error(message);
                        Thread.sleep(timeoutMilisecs); // The thread is forced to wait 'timeoutMilisecs' before trying to retry the FME call

                    }
                    count++;
                    LOGGER.info("Retry checking");
                    break;
                }
                case ABORTED:
                case FME_FAILURE:{
                    throw new GenericFMEexception("Received result status FME_FAILURE for job Id #" + jobId);}

                case SUCCESS:
                    return;
            }
        }
        throw new RetryCountForGettingJobResultReachedException("Retry count reached with no result");
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


    protected CloseableHttpClient getClient_() {
        return client_;
    }

    protected static RequestConfig.Builder getRequestConfigBuilder() {
        return requestConfigBuilder;
    }

    protected Integer getFmeTimeoutProperty() {
        return fmeTimeoutProperty;
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
