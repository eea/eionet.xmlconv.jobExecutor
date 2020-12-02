package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Properties;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpConnectionManagerFactory {
    private HttpConnectionManagerFactory() {
        // do nothing
    }

    private static PoolingHttpClientConnectionManager manager;

    public static PoolingHttpClientConnectionManager getInstance() {
        if (manager == null) {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(Properties.HTTP_MANAGER_TOTAL);
            cm.setDefaultMaxPerRoute(Properties.HTTP_MANAGER_ROUTE);
            manager = cm;
        }
        return manager;
    }

}
