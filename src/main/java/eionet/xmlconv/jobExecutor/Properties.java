package eionet.xmlconv.jobExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class Properties {
    public static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    public static String dateFormatPattern = "dd MMM yyyy";
    /**
     * Folder for XSLs.
     */
    public static String xslFolder = null;
    /**
     * Folder for temporary files.
     */
    public static String tmpFolder = null;
    /** Folder for QA scripts. */
    public static String queriesFolder = null;
    /** XGawk program executable command. */
    public static String xgawkCommand = null;
    /**
     * Data Dictionary URL, used when generating XSLs.
     */
    public static String ddURL = null;
    /**
     * Cache Configuration
     */
    public static String CACHE_TEMP_DIR;
    public static long CACHE_HTTP_SIZE;
    public static int CACHE_HTTP_EXPIRY;
    public static int HTTP_CACHE_ENTRIES;
    public static long HTTP_CACHE_OBJECTSIZE;
    public static int HTTP_SOCKET_TIMEOUT;
    public static int HTTP_CONNECT_TIMEOUT;
    public static int HTTP_MANAGER_TOTAL;
    public static int HTTP_MANAGER_ROUTE;

    /** Folder for XML files. */
    public static String xmlfileFolder = null;
    /** Folder for OpenDocument helper files. */
    public static String odsFolder = null;      //maybe this is not needed TODO
    /** Time pattern used for displaying time values on UI. */
    public static String timeFormatPattern = "dd MMM yyyy hh:mm:ss";

    /** FME host. */
    public static String fmeHost = null;
    /** FME port. */
    public static String fmePort = null;
    /** FME user login. */
    public static String fmeUser = null;
    /** FME user password. */
    public static String fmePassword = null;
    /** FME token. */
    public static String fmeToken = null;
    /** FME token expiration. */
    public static String fmeTokenExpiration = null;
    /** FME token timeunit. */
    public static String fmeTokenTimeunit = null;
    /** FME timeout. */
    public static int fmeTimeout = 0;
    public static int fmeRetryHours = 0;
    /** FME url for polling job status by job id. */
    public static String fmePollingUrl = null;
    public static String fmeResultFolderUrl = null;
    public static String fmeResultFolder = null;
    public static String fmeDeleteFolderUrl = null;



    static {
        // filesystem properties
        queriesFolder = "https://localhost:8078/queries";//getStringProperty("queries.folder");
        xslFolder = "https://localhost:8078/xsl"; //getStringProperty("xsl.folder");
        tmpFolder = "https://localhost:8078/tmp"; //= getStringProperty("tmp.folder");
        ddURL = "https://dd.ewxdevel1dub.eionet.europa.eu/"; //getStringProperty("dd.url");
        CACHE_TEMP_DIR = "/home/denia/Dev/EEA/eearun/cache";//getStringProperty("cache.temp.dir");
        CACHE_HTTP_SIZE = 2000; //getLongProperty("cache.http.size");
        CACHE_HTTP_EXPIRY = 120;//getIntProperty("cache.http.expiryinterval");
        HTTP_CACHE_ENTRIES = 1000;//getIntProperty("http.cache.entries");
        HTTP_CACHE_OBJECTSIZE = 524288000;//getLongProperty("http.cache.objectsize");
        HTTP_SOCKET_TIMEOUT = 30000;//getIntProperty("http.socket.timeout");
        HTTP_CONNECT_TIMEOUT = 30000;//getIntProperty("http.connect.timeout");
        HTTP_MANAGER_TOTAL = 200;//getIntProperty("http.manager.total");
        HTTP_MANAGER_ROUTE = 50;//getIntProperty("http.manager.route");
        xmlfileFolder = "https://localhost:8078/xmlfile";//getStringProperty("xmlfile.folder");
        timeFormatPattern = "dd MMM yyyy HH:mm";//getStringProperty("time.format.pattern");
        // exteranal QA program
        xgawkCommand = "xgawk";//getStringProperty("external.qa.command.xgawk");

        fmeHost = getStringProperty("fme.host");
        fmePort = getStringProperty("fme.port");
        fmeUser = getStringProperty("fme.user");
        fmePassword = getStringProperty("fme.password");
        fmeToken = getStringProperty("fme.token");
        fmeTokenExpiration = getStringProperty("fme.token.expiration");
        fmeTokenTimeunit = getStringProperty("fme.token.timeunit");
        fmeTimeout = getIntProperty("fme.timeout");
        fmeRetryHours = getIntProperty("fme.retry.hours");
        fmePollingUrl = getStringProperty("fme.polling.url");
        fmeResultFolderUrl = getStringProperty("fme.result.folder.url");
        fmeResultFolder = getStringProperty("fme.result.folder");
        fmeDeleteFolderUrl = getStringProperty("fme.delete.folder.url");
    }

    /**
     * Gets property value from key
     *
     * @param key Key
     * @return Value
     */
    public static String getStringProperty(String key) {
        //TODO
       /* try {
            return configurationService.resolveValue(key);
        } catch (CircularReferenceException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        } catch (UnresolvedPropertyException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }

        */
        return null;
    }

    /**
     * Gets property numeric value from key
     *
     * @param key Key
     * @return Value
     */
    private static int getIntProperty(String key) {
        String value = getStringProperty(key);

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            LOGGER.error(nfe.getMessage());
            return 0;
        }
    }

    private static long getLongProperty(String key) {
        String value = getStringProperty(key);

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException nfe) {
            LOGGER.error(nfe.getMessage());
            return 0L;
        }
    }

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
        return getStringProperty(key);
    }

    /**
     * Load message property with parameters from resource bundle.
     *
     * @param key         Resource bundle key.
     * @param replacement Replacement array.
     * @return
     */
    public static String getMessage(String key, Object[] replacement) {

        String message = MessageFormat.format(getMessage(key), replacement);
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