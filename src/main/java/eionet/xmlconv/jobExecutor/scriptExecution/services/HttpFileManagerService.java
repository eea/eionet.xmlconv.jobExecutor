package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.FollowRedirectException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public interface HttpFileManagerService {
    void getHttpResponse(HttpServletResponse response, String ticket, String url) throws IOException, URISyntaxException, ScriptExecutionException;
    CloseableHttpResponse getHeaderResponse(String ticket, String url, boolean isTrustedMode) throws IOException, ScriptExecutionException;
    long getSourceURLSize(String ticket, String url, boolean isTrustedMode);
    InputStream getInputStream(String srcUrl, String ticket, boolean isTrustedMode) throws IOException, URISyntaxException;
    InputStream getFileInputStream(String url, String ticket, boolean isTrustedMode) throws IOException, URISyntaxException, ScriptExecutionException;
    byte[] getFileByteArray(String url, String ticket, boolean isTrustedMode) throws URISyntaxException, ScriptExecutionException, IOException;
    void closeQuietly();
    URL followUrlRedirectIfNeeded(URL url) throws FollowRedirectException;
}
