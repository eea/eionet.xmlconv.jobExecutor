package eionet.xmlconv.jobExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.text.MessageFormat;

@Configuration
public class Properties {
    public static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    public static String dateFormatPattern = "dd MMM yyyy";
    /**
     * Folder for XSLs.
     */
    @Value( "${xsl.folder}" )
    public static String xslFolder;
    /**
     * Folder for temporary files.
     */
    @Value( "${tmp.folder}" )
    public static String tmpFolder;
    /** Folder for QA scripts. */
    @Value( "${queries.folder}" )
    public static String queriesFolder;
    /** XGawk program executable command. */
    @Value( "${external.qa.command.xgawk}" )
    public static String xgawkCommand;
    /**
     * Data Dictionary URL, used when generating XSLs.
     */
    @Value( "${dd.url}" )
    public static String ddURL = null;
    /**
     * Cache Configuration
     */
    @Value( "${cache.temp.dir}" )
    public static String CACHE_TEMP_DIR;
    @Value( "${cache.http.size}" )
    public static long CACHE_HTTP_SIZE;
    @Value( "${cache.http.expiryinterval}" )
    public static int CACHE_HTTP_EXPIRY;
    @Value( "${http.cache.entries}" )
    public static int HTTP_CACHE_ENTRIES;
    @Value( "${http.cache.objectsize}" )
    public static long HTTP_CACHE_OBJECTSIZE;
    @Value( "${http.socket.timeout}" )
    public static int HTTP_SOCKET_TIMEOUT;
    @Value( "${http.connect.timeout}" )
    public static int HTTP_CONNECT_TIMEOUT;
    @Value( "${http.manager.total}" )
    public static int HTTP_MANAGER_TOTAL;
    @Value( "${http.manager.route}" )
    public static int HTTP_MANAGER_ROUTE;

    /** Folder for XML files. */
    @Value( "${xmlfile.folder}" )
    public static String xmlfileFolder;
    /** Folder for OpenDocument helper files. */
    public static String odsFolder = null;      //maybe this is not needed TODO
    /** Time pattern used for displaying time values on UI. */
    @Value( "${time.format.pattern}" )
    public static String timeFormatPattern;

    /** FME host. */
    @Value( "${fme.host}" )
    public static String fmeHost;
    /** FME port. */
    @Value( "${fme.port}" )
    public static String fmePort;
    /** FME user login. */
    @Value( "${fme_user}" )
    public static String fmeUser;
    /** FME user password. */
    @Value( "${fme_user_password}" )
    public static String fmePassword;
    /** FME token. */
    @Value( "${fme_token}" )
    public static String fmeToken;
    /** FME token expiration. */
    @Value( "${fme_token_expiration}" )
    public static String fmeTokenExpiration;
    /** FME token timeunit. */
    @Value( "${fme_token_timeunit}" )
    public static String fmeTokenTimeunit;
    /** FME timeout. */
    @Value( "${fme_timeout=300000}" )
    public static int fmeTimeout;
    @Value( "${fme_retry_hours}" )
    public static int fmeRetryHours;
    /** FME url for polling job status by job id. */
    public static String fmePollingUrl;
    @Value( "${fme_result_folder_url}" )
    public static String fmeResultFolderUrl;
    @Value( "${fme_result_folder}" )
    public static String fmeResultFolder;
    @Value( "${fme_delete_folder_url}" )
    public static String fmeDeleteFolderUrl;
    @Value( "${job.execution.application.url}" )
    public static String jobExecutorApplicationUrl;

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

    /**
     * @param xmlfileFolder the xmlfileFolder to set
     */
    public static void setXmlfileFolder(String xmlfileFolder) {
        Properties.xmlfileFolder = xmlfileFolder;
    }

}