package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.utils.CacheManagerUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class HttpCacheClientFactory {
    private HttpCacheClientFactory() {
        // do nothing
    }

    @Autowired
    private static CacheManagerUtils cacheManagerUtils;

    private static CloseableHttpClient client;

    public static CloseableHttpClient getInstance(Environment environment) {
        if (client == null) {
            EhcacheHttpCacheStorage ehcacheHttpCacheStorage = new EhcacheHttpCacheStorage(cacheManagerUtils.getHttpCache(environment));
            CacheConfig cacheConfig = CacheConfig.custom()
                    .setSharedCache(false)
                    .setMaxCacheEntries(Integer.parseInt(environment.getProperty("http.cache.entries")))
                    .setMaxObjectSize(Long.parseLong(environment.getProperty("http.cache.objectsize")))
                    .build();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(Integer.parseInt(environment.getProperty("http.socket.timeout")))
                    .setConnectTimeout(Integer.parseInt(environment.getProperty("http.connect.timeout")))
                    .build();
            client = CachingHttpClients.custom()
                    .setCacheConfig(cacheConfig)
                    .setHttpCacheStorage(ehcacheHttpCacheStorage)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(HttpConnectionManagerFactory.getInstance(environment))
                    .setRetryHandler(new StandardHttpRequestRetryHandler())
                    .useSystemProperties()
                    .build();
        }
        return client;
    }
}
