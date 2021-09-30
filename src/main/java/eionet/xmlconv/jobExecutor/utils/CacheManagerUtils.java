package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import eionet.xmlconv.jobExecutor.datadict.DDDatasetTable;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.*;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Component("cacheManager")
public class CacheManagerUtils {

    /**
     * Public constructor
     */
    @Autowired
    public CacheManagerUtils() {
    }
    /**
     * Application (main) cache name.
     */
    public static final String APPLICATION_CACHE = "ApplicationCache";

    public static final String HTTP_CACHE = "http-cache";

    /** Data Dictionary tables data cache name. */
    private static final String DD_TABLES_CACHE = "ddTables";

    private static CacheManager cacheManager;

    /**
     * Updates Data Dictionary tables cache.
     * @param ddTables data dictionary tables
     */
    public static void updateDDTablesCache(final List<DDDatasetTable> ddTables) {


        getCacheManager((Environment)  SpringApplicationContext.getBean(Environment.class)).getCache(APPLICATION_CACHE).put(new Element(DD_TABLES_CACHE, ddTables));
    }

    /**
     * Returns last data dictionary tables entry.
     * @return last data dictionary tables entry.
     */
    public static List<DDDatasetTable> getDDTables() {
        Element element = getCacheManager((Environment)  SpringApplicationContext.getBean(Environment.class)).getCache(APPLICATION_CACHE) != null ? cacheManager.getCache(APPLICATION_CACHE).get(DD_TABLES_CACHE) : null;
        return element == null || element.getValue() == null ? Collections.EMPTY_LIST : (List<DDDatasetTable>) element.getValue();
    }

    public static Cache getHttpCache(Environment environment) {
        return getCacheManager(environment).getCache(HTTP_CACHE);
    }

    /**
     * Cache manager initializer. Used by Spring DI.
     */
    public static void initializeCacheManager(Environment environment) {
        if (cacheManager == null) {
            synchronized (CacheManager.class) {
                if (cacheManager == null) {
                    Configuration cacheManagerConfig = new Configuration()
                            .diskStore(new DiskStoreConfiguration()
                                    .path(environment.getProperty("cache.temp.dir")));
                    cacheManager = new CacheManager(cacheManagerConfig);
                    Cache appCache = new Cache(new CacheConfiguration(APPLICATION_CACHE, 2)
                            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                            .eternal(true));
                    cacheManager.addCache(appCache);
                    Cache httpCache = new Cache(new CacheConfiguration()
                            .name(HTTP_CACHE)
                            .maxEntriesLocalHeap(1)
                            .maxBytesLocalDisk(Long.parseLong(environment.getProperty("cache.http.size")), MemoryUnit.MEGABYTES)
                            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                            .diskExpiryThreadIntervalSeconds(Long.parseLong(environment.getProperty("cache.http.expiryinterval")))
                            .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP)));
                    cacheManager.addCache(httpCache);
                }
            }
        }
    }

    /**
     * Used to destroy the cache manager. Used by Spring DI.
     */
    public void destroyCacheManager() {
        getCacheManager((Environment)  SpringApplicationContext.getBean(Environment.class)).shutdown();
    }

    public static CacheManager getCacheManager(Environment environment){
        if (cacheManager == null)
        {
            initializeCacheManager(environment);
        }
        return cacheManager;
    }

}
