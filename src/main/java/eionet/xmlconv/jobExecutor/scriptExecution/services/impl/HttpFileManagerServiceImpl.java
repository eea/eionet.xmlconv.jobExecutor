package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import eionet.xmlconv.jobExecutor.exceptions.FollowRedirectException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.factories.HttpCacheClientFactory;
import eionet.xmlconv.jobExecutor.models.CustomURI;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.HttpFileManagerService;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;

@Service
@DependsOn({"cacheManager"})
public class HttpFileManagerServiceImpl implements HttpFileManagerService , EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileManagerService.class);
    private CloseableHttpClient client;
    private CloseableHttpResponse response;

    @Autowired
    private DataRetrieverService dataRetrieverService;

    Environment environment;

    @Autowired
    public HttpFileManagerServiceImpl() {
    }

    @PostConstruct
    void properlyInitialize(){
        LOGGER.info("Cache Temp Dir:"+  this.environment.getProperty("cache.temp.dir"));
         this.client = HttpCacheClientFactory.getInstance(this.environment);
    }

    public HttpFileManagerServiceImpl(CloseableHttpClient client){
        this.client = client;
    }

    /**
     * Fills outputstream with file content.
     * @param response Servlet response
     * @param ticket Authorization ticket
     * @param url File url
     * @throws IOException When an IO error occurs.
     * @throws URISyntaxException When the URL provided isn't a valid URI.
     */
    @Override
    public void getHttpResponse(HttpServletResponse response, String ticket, String url) throws IOException, URISyntaxException, ScriptExecutionException {
        CustomURI customURI = new CustomURI(url);
        String parsedURL = customURI.getHttpURL();
        HttpEntity entity = getFileEntity(parsedURL, ticket);

        String contentType = null;
        Header contentTypeHeader = entity.getContentType();
        if (contentTypeHeader != null) {
            contentType = contentTypeHeader.getValue();
        }
        long contentLength = entity.getContentLength();

        String contentEncoding = null;
        Header contentEncodingHeader = entity.getContentEncoding();
        if (contentEncodingHeader != null) {
            contentEncoding = contentEncodingHeader.getValue();
        }

        // If content type is null, then fall back to most likely content type
        if (contentType == null || "text/xml".equals(contentType)) {
            contentType = "text/xml;charset=utf-8";
        }
        // set response properties
        response.setContentType(contentType);
        response.setContentLengthLong(contentLength);
        /*response.setCharacterEncoding(contentEncoding);*/
        entity.writeTo(response.getOutputStream());
    }

    @Override
    public CloseableHttpResponse getHeaderResponse(String ticket, String url, boolean isTrustedMode) throws IOException, ScriptExecutionException {
        HttpHead httpHead = new HttpHead(url);

        if (  ! Utils.isNullStr(ticket) && isTrustedMode ) {
            httpHead.addHeader(HttpHeaders.AUTHORIZATION, " Basic " + ticket);
        }

        httpHead.addHeader(HttpHeaders.ACCEPT, "*/*");
        CloseableHttpResponse response = HttpClientBuilder.create().disableContentCompression().build().execute(httpHead);

        int statusCode = response.getStatusLine().getStatusCode();

        if ( statusCode == 200)
            return response;
        else
            throw new ScriptExecutionException("Remote file status code not ok: " + statusCode);

    }

    @Override
    public long getSourceURLSize(String ticket, String url, boolean isTrustedMode) {
        try {
            return Long.parseLong(getHeaderResponse(ticket, url, isTrustedMode).getFirstHeader("Content-Length").getValue() );
        } catch (Exception e) {
            LOGGER.error( "ERROR Retrieving Content Length for " + url , e );
            return -1;
        }
    }

    @Override
    public String getSourceUrlWithTicket(String ticket, String sourceUrl, boolean isTrustedMode) throws URISyntaxException, ScriptExecutionException {
        CustomURI uri = new CustomURI(sourceUrl);
        String parsedURL = uri.getHttpURL();
        if (Utils.isNullStr(ticket) && isTrustedMode) {
            ticket = getHostAuthentication(uri.getHost());
        }
        StringBuffer url = new StringBuffer();
        if (isTrustedMode && !Utils.isNullStr(ticket)) {
            /**
             * XXX: the purpose of the next line is to bypass any load balancer. Load balancers can have small timeouts which
             * can result in broken http transfers. We obviously need a better solution.
             */

            url.append(Properties.convertersUrl);
            url.append(Constants.GETSOURCE_URL);
            url.append("?");
            url.append(Constants.TICKET_PARAM);
            url.append("=");
            url.append(ticket);
            url.append("&");
            url.append(Constants.SOURCE_URL_PARAM);
            url.append("=");
            url.append(parsedURL);
        } else {
            url.append(parsedURL);
        }
        return url.toString();
    }

    /**
     * Opens stream to file url.
     * @param srcUrl File Url
     * @param ticket Authorization ticket
     * @param isTrustedMode Request is from a trusted source.
     * @return File input stream
     * @throws IOException When an IO error occurs.
     * @throws URISyntaxException When the URL provided isn't a valid URI.
     */
    @Override
    public InputStream getInputStream(String srcUrl, String ticket, boolean isTrustedMode) throws IOException, URISyntaxException {
        CustomURI customURL = new CustomURI(srcUrl);
        URL url = customURL.getRawURL();
        try {
            url = this.followUrlRedirectIfNeeded(url);
        } catch (FollowRedirectException e) {
            LOGGER.error( e.getMessage(), e.getCause() );
            throw new IOException("Failed to Redirect URL");
        }
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        if (ticket == null && isTrustedMode) {
            ticket = getHostAuthentication(customURL.getHost());
        }
        uc.addRequestProperty("Accept", "*/*");

        if (ticket != null) {
            // String auth = Utils.getEncodedAuthentication(user,pwd);
            uc.addRequestProperty("Authorization", " Basic " + ticket);
        }
        LOGGER.info("Opened stream to file: " + url.toString());
        InputStream stream = uc.getInputStream();

        return stream;
    }

    /**
     * Opens Stream to file URL
     * @param url File url
     * @param ticket Authorization ticket
     * @param isTrustedMode Request is from a trusted source.
     * @return File input stream
     * @throws IOException When an IO error occurs.
     * @throws URISyntaxException When the URL provided isn't a valid URI.
     */
    @Override
    public InputStream getFileInputStream(String url, String ticket, boolean isTrustedMode) throws IOException, URISyntaxException, ScriptExecutionException {
        CustomURI customURL = new CustomURI(url);
        String parsedUrl = customURL.getHttpURL();
        if (ticket == null && isTrustedMode) {
            ticket = getHostAuthentication(customURL.getHost());
        }
        HttpEntity entity = getFileEntity(parsedUrl, ticket);
        if (entity != null) {
            return entity.getContent();
        }
        return null;
    }

    @Override
    public byte[] getFileByteArray(String url, String ticket, boolean isTrustedMode) throws URISyntaxException, ScriptExecutionException, IOException {
        CustomURI customURL = new CustomURI(url);
        String parsedUrl = customURL.getHttpURL();
        if (ticket == null && isTrustedMode) {
            ticket = getHostAuthentication(customURL.getHost());
        }
        HttpEntity entity = getFileEntity(parsedUrl, ticket);
        if (entity != null) {
            return EntityUtils.toByteArray(entity);
        }
        return null;
    }

    /**
     * Returns response entity that should include the requested file content.
     * @param url File url
     * @param ticket Authorization ticket
     * @return Entity
     * @throws IOException When an IO error occurs.
     * @throws URISyntaxException When the URL provided isn't a valid URI.
     */
    private HttpEntity getFileEntity(String url, String ticket) throws IOException, URISyntaxException {
        if (StringUtils.contains(url, Constants.SOURCE_URL_PARAM)) {
            LOGGER.error("File proxy URL detected: " + url);
            throw new URISyntaxException(url, "File proxy URL detected, aborting download");
        }
        LOGGER.info("Start to download file: " + url);
        HttpCacheContext context = HttpCacheContext.create();
        HttpGet httpget = new HttpGet(url);
        if (ticket != null) {
            httpget.addHeader(HttpHeaders.AUTHORIZATION, " Basic " + ticket);
        }
        httpget.addHeader(HttpHeaders.ACCEPT, "*/*");
        httpget.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
        response = client.execute(httpget, context);

        CacheResponseStatus responseStatus = context.getCacheResponseStatus();
        switch (responseStatus) {
            case VALIDATED:
                LOGGER.info("The response was generated from the cache after validating the entry with the origin server.");
                break;
            case CACHE_MISS:
                LOGGER.info("Entry not found in cache.");
                break;
            default:
                LOGGER.info("Response from cache: " + responseStatus);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpServletResponse.SC_OK) {
            return response.getEntity();
        } else {
            throw new IOException("The file could not be retrieved");
        }
    }

    @Override
    public void closeQuietly() {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            LOGGER.error("Could not close resource: " + e);
        }
    }

    private String getHostAuthentication(String host) {
        try {
            return dataRetrieverService.getHostAuthentication(host);
        } catch (Exception e) {
            LOGGER.error("Error when retrieving host authentication from converters " + e.getMessage());
            LOGGER.error("Conversion proceeded");
        }
        return null;
    }

    /**
     * @param url the Url to check for redirection
     * @return url the Url redirected if required
     * */
    @Override
    public URL followUrlRedirectIfNeeded(URL url) throws FollowRedirectException {

        LOGGER.info("Checking URL:"+ url.toString()+" for redirects.");
        HttpGet request = new HttpGet(url.toString());
        try {
            HttpURLConnection con = (HttpURLConnection)(url.openConnection());
            con.setInstanceFollowRedirects(false);
            con.connect();
            int responseCode = con.getResponseCode();
            LOGGER.info("URL Redirection Mechanism Response Code :"+ responseCode);
            if(responseCode==301 || responseCode == 302){
                String location = con.getHeaderField( "Location" );
                LOGGER.info("Redirect Location is:"+location);
                return new URL(con.getHeaderField("Location"));
            }
        } catch (IOException e) {
            throw new FollowRedirectException("Error trying to invoke Server with Url:"+url.toString(),e.getCause());
        }
        return url;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment= environment;
    }
}
