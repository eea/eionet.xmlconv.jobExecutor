package eionet.xmlconv.jobExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;

@Configuration
public class Properties {
    public static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    public static String dateFormatPattern = "dd MMM yyyy";
    /**
     * Folder for XSLs.
     */
    public static String xslFolder;
    /**
     * Folder for temporary files.
     */
    public static String tmpFolder;
    /** Folder for QA scripts. */
    public static String queriesFolder;
    /**
     * Data Dictionary URL, used when generating XSLs.
     */
    public static String ddURL = null;
    /**
     * Cache Configuration
     */
    public static String CACHE_TEMP_DIR;
    public static long CACHE_HTTP_SIZE ;
    public static int CACHE_HTTP_EXPIRY;
    public static int HTTP_CACHE_ENTRIES = 1000;
    public static long HTTP_CACHE_OBJECTSIZE = 524288000;
    public static int HTTP_SOCKET_TIMEOUT;
    public static int HTTP_CONNECT_TIMEOUT;
    public static int HTTP_MANAGER_TOTAL;
    public static int HTTP_MANAGER_ROUTE;

    /** Folder for XML files. */
    public static String xmlfileFolder;
    /** Folder for OpenDocument helper files. */
    public static String odsFolder = null;
    /** Time pattern used for displaying time values on UI. */
    public static String timeFormatPattern;

    /** FME host. */
    public static String fmeHost;
    /** FME port. */
    public static String fmePort;
    /** FME user login. */
    public static String fmeUser;
    /** FME user password. */
    public static String fmePassword;
    /** FME asynchronous token. */
    public static String fmeToken;
    /** FME synchronous token. */
    public static String fmeSynchronousToken;
    /** FME token expiration. */
    public static String fmeTokenExpiration;
    /** FME token timeunit. */
    public static String fmeTokenTimeunit;
    /** FME timeout. */
    public static int fmeTimeout;
    public static int fmeSocketTimeout;
    public static int fmeRetryHours;
    /** FME url for polling job status by job id. */
    public static String fmePollingUrl;
    public static String fmeResultFolderUrl;
    public static String fmeResultFolder;
    public static String fmeDeleteFolderUrl;

    /** XSL folder for generated conversions. */
    public static String metaXSLFolder;
    /** conversion.xml file location, listing all available generated conversions. */
    public static String convFile;
    public static String convertersUrl;

    public static String ddReleaseInfoUrl;
    public static String convertersSchemaRetrievalUrl;
    public static String convertersHostAuthenticationUrl;
    public static String convertersEndpointToken;
    public static String ddEndpointToken;
    public static Long responseTimeoutMs;
    public static Integer rancherJobExecutorType;
    public static String RANCHER_POD_NAME;

    /**
     * Checks path
     *
     * @param path Path
     * @return Removes trailing slash
     */
    private static String checkPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Load message property from resource bundle.
     *
     * @param key Resource bundle key.
     * @return String value.
     */
    public static String getMessage(String key) {
        return key;
    }

    /**
     * Load message property with parameters from resource bundle.
     *
     * @param key         Resource bundle key.
     * @param replacement Replacement array.
     * @return
     */
    public static String getMessage(String key, Object[] replacement) {

        String message = MessageFormat.format(key, replacement);
        if (message != null) {
            return message;
        }
        return null;
    }
    /**
     * @return the xslFolder
     */
    public static String getXslFolder() {
        return xslFolder;
    }

    /**
     * @return the tmpFolder
     */
    public static String getTmpFolder() {
        return tmpFolder;
    }

    //setters These are needed in order to inject the static fields

    @Value( "${xmlfile.folder}" )
    public void setXmlfileFolder(String xmlfileFolder) {
        this.xmlfileFolder = xmlfileFolder;
    }

    @Value( "${xsl.folder}" )
    public void setXslFolder(String xslFolder) {
        this.xslFolder = xslFolder;
    }

    @Value( "${tmp.folder}" )
    public void setTmpFolder(String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    @Value( "${queries.folder}" )
    public void setQueriesFolder(String queriesFolder) {
        this.queriesFolder = queriesFolder;
    }

    @Value( "${dd.url}" )
    public void setDdURL(String ddURL) {
        this.ddURL = ddURL;
    }
    @Value( "${cache.temp.dir}" )
    public void setCacheTempDir(String cacheTempDir) {
        this.CACHE_TEMP_DIR = cacheTempDir;
    }

    @Value( "${cache.http.size}" )
    public void setCacheHttpSize(long cacheHttpSize) {
        this.CACHE_HTTP_SIZE = cacheHttpSize;
    }

    @Value( "${cache.http.expiryinterval}" )
    public void setCacheHttpExpiry(int cacheHttpExpiry) {
        this.CACHE_HTTP_EXPIRY = cacheHttpExpiry;
    }

    @Value( "${http.cache.entries}" )
    public void setHttpCacheEntries(int httpCacheEntries) {
        this.HTTP_CACHE_ENTRIES = httpCacheEntries;
    }

    @Value( "${http.cache.objectsize}" )
    public void setHttpCacheObjectsize(long httpCacheObjectsize) {
        this.HTTP_CACHE_OBJECTSIZE = httpCacheObjectsize;
    }

    @Value( "${http.socket.timeout}" )
    public void setHttpSocketTimeout(int httpSocketTimeout) {
        this.HTTP_SOCKET_TIMEOUT = httpSocketTimeout;
    }

    @Value( "${http.connect.timeout}" )
    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.HTTP_CONNECT_TIMEOUT = httpConnectTimeout;
    }

    @Value( "${http.manager.total}" )
    public void setHttpManagerTotal(int httpManagerTotal) {
        this.HTTP_MANAGER_TOTAL = httpManagerTotal;
    }

    @Value( "${http.manager.route}" )
    public void setHttpManagerRoute(int httpManagerRoute) {
        this.HTTP_MANAGER_ROUTE = httpManagerRoute;
    }

    @Value( "${time.format.pattern}" )
    public void setTimeFormatPattern(String timeFormatPattern) {
        this.timeFormatPattern = timeFormatPattern;
    }

    @Value( "${fme.host}" )
    public void setFmeHost(String fmeHost) {
        this.fmeHost = fmeHost;
    }

    @Value( "${fme.port}" )
    public void setFmePort(String fmePort) {
        this.fmePort = fmePort;
    }

    @Value( "${fme_user}" )
    public void setFmeUser(String fmeUser) {
        this.fmeUser = fmeUser;
    }

    @Value( "${fme_user_password}" )
    public void setFmePassword(String fmePassword) {
        this.fmePassword = fmePassword;
    }

    @Value( "${fme_token}" )
    public void setFmeToken(String fmeToken) {
        this.fmeToken = fmeToken;
    }

    @Value( "${fme_synchronous_token}" )
    public void setFmeSynchronousToken(String fmeSynchronousToken) {
        this.fmeSynchronousToken = fmeSynchronousToken;
    }

    @Value( "${fme_token_expiration}" )
    public void setFmeTokenExpiration(String fmeTokenExpiration) {
        this.fmeTokenExpiration = fmeTokenExpiration;
    }

    @Value( "${fme_token_timeunit}" )
    public void setFmeTokenTimeunit(String fmeTokenTimeunit) {
        this.fmeTokenTimeunit = fmeTokenTimeunit;
    }

    @Value( "${fme_timeout}" )
    public void setFmeTimeout(int fmeTimeout) {
        this.fmeTimeout = fmeTimeout;
    }

    @Value( "${fme_socket_timeout}" )
    public void setFmeSocketTimeout(int fmeSocketTimeout) {
        this.fmeSocketTimeout = fmeSocketTimeout;
    }

    @Value( "${fme_retry_hours}" )
    public void setFmeRetryHours(int fmeRetryHours) {
        this.fmeRetryHours = fmeRetryHours;
    }

    @Value( "${fme_result_folder_url}" )
    public void setFmeResultFolderUrl(String fmeResultFolderUrl) {
        this.fmeResultFolderUrl = fmeResultFolderUrl;
    }

    @Value( "${fme_result_folder}" )
    public void setFmeResultFolder(String fmeResultFolder) {
        this.fmeResultFolder = fmeResultFolder;
    }

    @Value( "${fme_delete_folder_url}" )
    public void setFmeDeleteFolderUrl(String fmeDeleteFolderUrl) {
        this.fmeDeleteFolderUrl = fmeDeleteFolderUrl;
    }

    @Value( "${fme_polling_url}" )
    public void setFmePollingUrl(String fmePollingUrl) {
        this.fmePollingUrl = fmePollingUrl;
    }

    @Value( "${ods.folder}" )
    public void setOdsFolder(String odsFolder) {
        this.odsFolder = odsFolder;
    }

    @Value( "${env.converters.url}" )
    public void setConvertersUrl(String convertersUrl) {
        this.convertersUrl = convertersUrl;
    }

    @Value( "${dd.release.info.url}" )
    public void setDdReleaseInfoUrl(String ddReleaseInfoUrl) {
        this.ddReleaseInfoUrl = ddReleaseInfoUrl;
    }

    @Value( "${converters.schema.retrieval.url}" )
    public  void setConvertersSchemaRetrievalUrl(String convertersSchemaRetrievalUrl) {
        this.convertersSchemaRetrievalUrl = convertersSchemaRetrievalUrl;
    }

    @Value( "${converters.host.authentication.url}" )
    public void setConvertersHostAuthenticationUrl(String convertersHostAuthenticationUrl) {
        this.convertersHostAuthenticationUrl = convertersHostAuthenticationUrl;
    }

    @Value( "${converters.restapi.token}" )
    public void setConvertersEndpointToken(String convertersEndpointToken) {
        this.convertersEndpointToken = convertersEndpointToken;
    }

    @Value( "${dd.restapi.token}" )
    public void setDdEndpointToken(String ddEndpointToken) {
        this.ddEndpointToken = ddEndpointToken;
    }

    @Value( "${response.timeout.ms}" )
    public void setResponseTimeoutMs(Long responseTimeoutMs) {
        this.responseTimeoutMs = responseTimeoutMs;
    }

    @Value("${rancher.jobExecutor.type}")
    public void setRancherJobExecutorType(Integer rancherJobExecutorType) {
        this.rancherJobExecutorType = rancherJobExecutorType;
    }

    @Value("${rancher.pod.name}")
    public void setRancherPodName(String rancherPodName) {
        this.RANCHER_POD_NAME = rancherPodName;
    }

}