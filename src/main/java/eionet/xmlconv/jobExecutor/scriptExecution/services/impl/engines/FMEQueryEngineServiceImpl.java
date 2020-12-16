package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeJobStatus;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeServerCommunicator;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SynchronousSubmitJobRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import eionet.xmlconv.jobExecutor.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("fmeEngineService")
public class FMEQueryEngineServiceImpl extends ScriptEngineServiceImpl{

    private static final Logger LOGGER = LoggerFactory.getLogger(FMEQueryEngineServiceImpl.class);

    private CloseableHttpClient client_ = null;

    private static RequestConfig.Builder requestConfigBuilder = null;

    private String fmeUrl = null;

    private Integer retries = 0;

    private String randomStr = RandomStringUtils.randomAlphanumeric(5);


    /* Variables for eionet.gdem.Properties*/
    private Integer fmeTimeoutProperty = Properties.fmeTimeout;
    private String fmeHostProperty = Properties.fmeHost;
    private String fmePortProperty = Properties.fmePort;
    private String fmeTokenExpirationProperty = Properties.fmeTokenExpiration;
    private String fmeTokenTimeunitProperty = Properties.fmeTokenTimeunit;
    private String fmePollingUrlProperty = Properties.fmePollingUrl;
    private Integer fmeRetryHoursProperty = Properties.fmeRetryHours;
    private String fmeTokenProperty = Properties.fmeToken;

    /**
     * Default constructor.
     * @throws Exception If an error occurs.
     */
    @Autowired
    public FMEQueryEngineServiceImpl() throws Exception {
        client_ = HttpClients.createDefault();

        requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(this.getFmeTimeoutProperty());
    }

    @Override
    protected void runQuery(Script script, OutputStream result) throws IOException {
        String[] urlSegments = script.getOrigFileUrl().split("/");
        String fileNameWthXml = urlSegments[urlSegments.length-1];
        String[] fileNameSegments = fileNameWthXml.split("\\.");
        String fileName = fileNameSegments[0];
        String folderName = fileName + "_" +  getRandomStr();
        try {


            FmeServerCommunicator fmeServerCommunicator = this.getFmeServerCommunicator();
            String jobId =     fmeServerCommunicator.submitJob(script,new SynchronousSubmitJobRequest(script.getOrigFileUrl(),folderName));


            this.pollFmeServerWithRetries(jobId,script,fmeServerCommunicator);

            fmeServerCommunicator.getResultFiles(folderName, result);
            fmeServerCommunicator.deleteFolder(folderName);
        } catch (FmeAuthorizationException | FmeCommunicationException | GenericFMEexception | FMEBadRequestException |RetryCountForGettingJobResultReachedException | InterruptedException e) {
            String message = "Generic Exception handling. FME request error: " + e.getMessage();
            LOGGER.error(message);
            String resultString ="<div class=\"feedbacktext\"><span id=\"feedbackStatus\" class=\"BLOCKER\" style=\"display:none\">The QC process failed. Please try again. If the issue persists please contact the dataflow helpdesk.</span>The QC process failed. Please try again. If the issue persists please contact the dataflow helpdesk.</div>";
            ZipOutputStream out = new ZipOutputStream(result);
            ZipEntry entry = new ZipEntry("output.html");
            out.putNextEntry(entry);
            byte[] data = resultString.getBytes();
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
        while (count < this.getRetries()) {
            LOGGER.info(String.format("Retry %d for polling for status of job #%s", count, jobId));
            FmeJobStatus jobStatus = fmeServerCommunicator.getJobStatus(jobId,script);
            switch (jobStatus){
                case SUBMITTED:
                case PULLED:
                case QUEUED: {
                    if (count + 1 == this.getRetries()) {
                        String message = "Failed for last Retry  number: " + count + ". Received status " + jobStatus.toString();
                        throw new RetryCountForGettingJobResultReachedException(message);
                    } else {
                        LOGGER.error("Fme Request Process is still in progress for  -- Source file: " + script.getOrigFileUrl() + " -- FME workspace: " + script.getScriptSource() + " -- Response: " + jobStatus.toString() + "-- #Retry: " + count);
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
        return randomStr;
    }

    public FmeServerCommunicator getFmeServerCommunicator(){
        return (FmeServerCommunicator) SpringApplicationContext.getBean(FmeServerCommunicator.class);
    }

}
