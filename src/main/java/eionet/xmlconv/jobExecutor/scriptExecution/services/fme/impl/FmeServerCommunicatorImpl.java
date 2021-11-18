package eionet.xmlconv.jobExecutor.scriptExecution.services.fme.impl;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.ApacheHttpClientUtils;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.ApacheHttpClientWrapper;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeJobStatus;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.FmeServerCommunicator;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.HttpRequestHeader;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SubmitJobRequest;
import eionet.xmlconv.jobExecutor.utils.Utils;
import eionet.xmlconv.jobExecutor.utils.ZipUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class FmeServerCommunicatorImpl implements FmeServerCommunicator {

    private Environment env;

    private static final Logger LOGGER = LoggerFactory.getLogger(FmeServerCommunicatorImpl.class);
    private String fmeTokenProperty;
    private String fmePollingUrl;
    private String fmeResultFolderUrlProperty;
    private String fmeResultFolderProperty;
    private String tmpFolderProperty;
    private String fmeDeleteFolderUrlProperty;

    private ApacheHttpClientWrapper clientWrapper;
    private static final String JSON_STATUS_PARAM="status";
    private static final String FME_TOKEN_HEADER="fmetoken token=";
    private static final String JSON_JOB_ID_PARAM="id";
    private static final String MEDIA_TYPE_JSON="application/json";
    private static final String APPLICATION_ZIP="application/zip";
    private static final String CONTENT_TYPE_FORM_URLENCODED="application/x-www-form-urlencoded";

    @Autowired
    public FmeServerCommunicatorImpl(@Qualifier("fmeApacheHttpClient") ApacheHttpClientWrapper clientWrapper, Environment env) {
        this.clientWrapper = clientWrapper;

        this.env = env;

        this.fmeTokenProperty = this.env.getProperty("fme_token");
        this.fmePollingUrl = this.env.getProperty("fme_polling_url");
        this.fmeResultFolderUrlProperty = this.env.getProperty("fme_result_folder_url");
        this.fmeResultFolderProperty = this.env.getProperty("fme_result_folder");
        this.tmpFolderProperty = this.env.getProperty("tmp.folder") + File.separatorChar;
        this.fmeDeleteFolderUrlProperty = this.env.getProperty("fme_delete_folder_url");
    }

    @Override
    public String submitJob(Script script, SubmitJobRequest submitJobRequest) throws FmeAuthorizationException, FmeCommunicationException {
        if (script == null) {
            throw new IllegalArgumentException("XQScript is empty");
        } else {
            if (script.getScriptSource() == null) {
                throw new IllegalArgumentException("XQScript source file is empty");
            }
            if (script.getOrigFileUrl() == null) {
                throw new IllegalArgumentException("XML file was not provided");
            }
        }
        String convertersJobId = script.getJobId();
        String message = "Began asynchronous job submission in FME for script " + script.getScriptSource();
        if (!Utils.isNullStr(convertersJobId)){
            message += " Converters JobId is " + convertersJobId;
        }
        LOGGER.info(message);
        HttpPost postMethod = null;
        CloseableHttpResponse response = null;
        String jobId = null;
        try {

            postMethod = new HttpPost(new URI(script.getScriptSource()));
            Header[] headers = new HttpRequestHeader.Builder().createHeader(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_JSON).
                    createHeader(HttpHeaders.ACCEPT, MEDIA_TYPE_JSON).createHeader(HttpHeaders.AUTHORIZATION, FME_TOKEN_HEADER + fmeTokenProperty).build().getHeaders();
            postMethod.setHeaders(headers);

            LOGGER.info("For jobId " + convertersJobId + " a POST request will be made to FME to asynchronously submit a job. Url: " + script.getScriptSource() + ". Headers are: " + Arrays.toString(headers) );

            if(Utils.isNullStr(fmeTokenProperty)){
                LOGGER.info("For jobId " + convertersJobId + " fme token is empty.");
            }

            StringEntity params6 = new StringEntity(submitJobRequest.buildBody());
            postMethod.setEntity(params6);

            response = this.clientWrapper.getClient().execute(postMethod);
            LOGGER.info("For jobId " + convertersJobId + " response: " + response.toString());

            Integer statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new FmeAuthorizationException("Unauthorized token");
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new FmeCommunicationException("The workspace or repository does not exist");
            } else if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                throw new FmeCommunicationException("Some or all of the input parameters are invalid");
            } else {
                if (statusCode != HttpStatus.SC_ACCEPTED) {
                    String errorMsg = "Received status code " + statusCode + " for job submission request.";
                    if (!Utils.isNullStr(convertersJobId)){
                        errorMsg += " Converters JobId is " + convertersJobId;
                    }
                    throw new FmeCommunicationException(errorMsg);
                }
            }

            JSONObject jsonResponse = ApacheHttpClientUtils.getJsonFromResponseEntity(response.getEntity());
            jobId = jsonResponse.get(JSON_JOB_ID_PARAM).toString();
            if (jobId == null || jobId.isEmpty() || jobId.equals("null")) {
                throw new FmeCommunicationException("Valid status code but no job ID was retrieved");
            }
            String logMessage = "Job was submitted in FME for script " + script.getScriptSource() + " with FME job id " + jobId;
            if (!Utils.isNullStr(convertersJobId)){
                logMessage += " Converters JobId is " + convertersJobId;
            }
            LOGGER.info(logMessage);

        } catch (URISyntaxException | HttpRequestHeaderInitializationException |IOException e) {
            throw new FmeCommunicationException(e.getMessage());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return jobId;
    }

    @Override
    public FmeJobStatus getJobStatus(String jobId, Script script) throws FmeAuthorizationException, FmeCommunicationException ,GenericFMEexception ,FMEBadRequestException{
        HttpGet getMethod = null;
        CloseableHttpResponse response = null;

        try {
            getMethod = new HttpGet(new URI(this.fmePollingUrl + jobId));
            Header[] headers = new HttpRequestHeader.Builder().createHeader(HttpHeaders.ACCEPT, MEDIA_TYPE_JSON).
                    createHeader(HttpHeaders.AUTHORIZATION, FME_TOKEN_HEADER + fmeTokenProperty).build().getHeaders();
            getMethod.setHeaders(headers);
            LOGGER.info("For jobId " + script.getJobId() + " a GET request will be made to FME to asynchronously poll for status of FME job "+ jobId + " . Url: " + this.fmePollingUrl + jobId + ". Headers are: " + Arrays.toString(headers) );

            response = this.clientWrapper.getClient().execute(getMethod);
            LOGGER.info("For jobId " + script.getJobId() + " response of polling for status is " + response.toString());

            Integer statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new FmeAuthorizationException("Unauthorized token");
            }else if(statusCode == HttpStatus.SC_NOT_FOUND){
                throw new FMEBadRequestException("The job does not exist");

            } else if (statusCode == HttpStatus.SC_OK) {
                JSONObject jsonResponse = ApacheHttpClientUtils.getJsonFromResponseEntity(response.getEntity());
                if (jsonResponse.get(JSON_STATUS_PARAM) != null) {
                    return FmeJobStatus.valueOf(jsonResponse.get(JSON_STATUS_PARAM).toString());
                } else {
                    String message = "Received wrong response status " + jsonResponse.get("status");
                    throw new FmeCommunicationException(message);
                }
            }else {
                // NOT Valid status code
                String message = "Error when polling for job status. Received status code: " + statusCode + ". Response: " + response.toString();
                throw new GenericFMEexception(message);
            }
        } catch (URISyntaxException | HttpRequestHeaderInitializationException | IOException e) {
            LOGGER.error("For jobId " + script.getJobId() + " exception message is: " + e.getMessage());
            throw new GenericFMEexception(e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    @Override
    public void getResultFiles(String jobId, String folderName, String resultFile) throws FmeAuthorizationException  , FMEBadRequestException , GenericFMEexception {
        LOGGER.info("For jobId " + jobId + " began downloading folder " + folderName);
        HttpPost postMethod = null;
        CloseableHttpResponse response = null;
        try {

            postMethod = new HttpPost(new URI(fmeResultFolderUrlProperty + fmeResultFolderProperty + "/" + folderName));

            Header[] headers = new HttpRequestHeader.Builder().createHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED).createHeader(HttpHeaders.ACCEPT, APPLICATION_ZIP).
                    createHeader(HttpHeaders.AUTHORIZATION, FME_TOKEN_HEADER + fmeTokenProperty).build().getHeaders();
            postMethod.setHeaders(headers);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("folderNames","."));
            nameValuePairs.add(new BasicNameValuePair("zipFileName","htmlfiles.zip"));
            postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            LOGGER.info("For jobId " + jobId + " a POST request will be made to FME to asynchronously retrieve the zip with the results.  Url: " + fmeResultFolderUrlProperty + fmeResultFolderProperty + "/" + folderName
                    + ". Headers are: " + Arrays.toString(headers) + " Parameters are [folderNames=.], [zipFileName=htmlfiles.zip]");


            response = this.clientWrapper.getClient().execute(postMethod);
            LOGGER.info("For jobId " + jobId + " response of for retrieving zip is " + response.toString());
            Integer statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_UNAUTHORIZED){
                throw new FmeAuthorizationException("Unauthorized token");
            }
            else if (statusCode == HttpStatus.SC_NOT_FOUND){
                throw new FMEBadRequestException("The resource connection or directory does not exist");
            }
            else if (statusCode == HttpStatus.SC_CONFLICT){
                throw new FMEBadRequestException("The resource connection is not a type of resource that can be downloaded");
            }
            else {
                if (statusCode != HttpStatus.SC_OK){
                    String message = "Received status code " + statusCode + " for folder downloading";
                    throw new FMEBadRequestException(message);
                }
            }
            //status code is HttpStatus.SC_OK (200)
            LOGGER.info("For jobId " + jobId + " Received status code 200 when downloading folder " + folderName + " Response: " + response.toString());

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            //Store zip file in tmp folder
            String folderPath =  tmpFolderProperty + folderName;
            FileOutputStream fos = new FileOutputStream(new File(folderPath+".zip"));
            int inByte;
            while((inByte = is.read()) != -1)
                fos.write(inByte);
            fos.close();

            File zipFile = new File(folderPath+".zip");
            FileOutputStream result = new FileOutputStream(new File(resultFile));
            IOUtils.copy(new FileInputStream(zipFile),result);
            LOGGER.info("For jobId " + jobId + " copied content of " + folderName + ".zip" + " to stream");
            Utils.deleteFolder(folderPath+".zip");
            LOGGER.info("For jobId " + jobId + " deleted folder " + folderPath + ".zip");
            LOGGER.info("For jobId " + jobId + " finished downloading folder " + folderName + " from FME");

        }  catch (URISyntaxException | HttpRequestHeaderInitializationException | IOException e) {
            throw new GenericFMEexception(e.getMessage());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    @Override
    public void deleteFolder (String jobId, String folderName) throws FmeAuthorizationException , GenericFMEexception  ,FMEBadRequestException{
        LOGGER.info("For jobId " + jobId + " began deleting folder " + folderName);
        HttpDelete request = null;
        CloseableHttpResponse response = null;
        try {
            java.net.URI uri = new URIBuilder(fmeDeleteFolderUrlProperty + fmeResultFolderProperty + "/" + folderName)
                    .build();
            request = new HttpDelete(uri);
            Header[] headers = new HttpRequestHeader.Builder().createHeader(HttpHeaders.ACCEPT, MEDIA_TYPE_JSON).
                    createHeader(HttpHeaders.AUTHORIZATION, FME_TOKEN_HEADER + fmeTokenProperty).build().getHeaders();
            request.setHeaders(headers);
            LOGGER.info("For jobId " + jobId + " a DELETE request will be made to FME to asynchronously delete result folder "+ folderName + " . Url: " + uri.toURL().toString() + ". Headers are: " + Arrays.toString(headers) );

            response = this.clientWrapper.getClient().execute(request);
            LOGGER.info("For jobId " + jobId + " response of deleting folder " + folderName + " is: " + response.toString());
            Integer statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_UNAUTHORIZED){
                throw new FmeAuthorizationException("Unauthorized token");
            }
            else if (statusCode == HttpStatus.SC_NOT_FOUND){
                throw new FMEBadRequestException("The resource connection or path does not exist");
            }
            else {
                if (statusCode != HttpStatus.SC_NO_CONTENT){
                    String message = "Received status code " + statusCode + " for folder deletion";
                    throw new GenericFMEexception(message);
                }
            }
            //status code is HttpStatus.SC_NO_CONTENT (204)
            LOGGER.info("For jobId " + jobId + " deleted folder " + folderName);
        }  catch (GenericFMEexception | URISyntaxException | HttpRequestHeaderInitializationException |IOException e) {
            throw new GenericFMEexception(e.getMessage());
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
    }
}
