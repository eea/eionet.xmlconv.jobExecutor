package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Properties;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.core.env.Environment;

public class HttpConnectionManagerFactory {
    private HttpConnectionManagerFactory() {
        // do nothing
    }

    private static PoolingHttpClientConnectionManager manager;

    public static PoolingHttpClientConnectionManager getInstance(Environment environment) {
        if (manager == null) {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(Integer.parseInt(environment.getProperty("http.manager.total")));
            cm.setDefaultMaxPerRoute(Integer.parseInt(environment.getProperty("http.manager.route")));
            manager = cm;
        }
        return manager;
    }

}
