package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.utils.CacheManagerUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;

public class HttpCacheClientFactory {
    private HttpCacheClientFactory() {
        // do nothing
    }

    private static CloseableHttpClient client;

    public static CloseableHttpClient getInstance() {
        if (client == null) {
            EhcacheHttpCacheStorage ehcacheHttpCacheStorage = new EhcacheHttpCacheStorage(CacheManagerUtils.getHttpCache());
            CacheConfig cacheConfig = CacheConfig.custom()
                    .setSharedCache(false)
                    .setMaxCacheEntries(Properties.HTTP_CACHE_ENTRIES)
                    .setMaxObjectSize(Properties.HTTP_CACHE_OBJECTSIZE)
                    .build();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(Properties.HTTP_SOCKET_TIMEOUT)
                    .setConnectTimeout(Properties.HTTP_CONNECT_TIMEOUT)
                    .build();
            client = CachingHttpClients.custom()
                    .setCacheConfig(cacheConfig)
                    .setHttpCacheStorage(ehcacheHttpCacheStorage)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(HttpConnectionManagerFactory.getInstance())
                    .setRetryHandler(new StandardHttpRequestRetryHandler())
                    .useSystemProperties()
                    .build();
        }
        return client;
    }
}
