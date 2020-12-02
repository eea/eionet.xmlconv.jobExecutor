package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import org.apache.http.impl.client.CloseableHttpClient;

public interface ApacheHttpClientWrapper {


    CloseableHttpClient getClient();
}
