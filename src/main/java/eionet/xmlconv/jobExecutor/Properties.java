package eionet.xmlconv.jobExecutor;

import java.text.MessageFormat;

public class Properties {
    public static String dateFormatPattern = "dd MMM yyyy";
    /** Folder for XSLs. */
    public static String xslFolder = null;
    /** Folder for temporary files. */
    public static String tmpFolder = null;
    /** Data Dictionary URL, used when generating XSLs. */
    public static String ddURL = null;



    static {
        xslFolder = "https://localhost:8080/xsl"; //getStringProperty("xsl.folder");
        tmpFolder = "https://localhost:8080/tmp"; //= getStringProperty("tmp.folder");
        ddURL = "https://dd.ewxdevel1dub.eionet.europa.eu/"; //getStringProperty("dd.url");
    }

    /**
     * Load message property with parameters from resource bundle.
     *
     * @param key
     *            Resource bundle key.
     * @param replacement
     *            Replacement array.
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
     * Load message property from resource bundle.
     *
     * @param key
     *            Resource bundle key.
     * @return String value.
     */
    public static String getMessage(String key) {
        return getStringProperty(key);
    }

    /**
     * Gets property value from key
     * @param key Key
     * @return Value
     */
    public static String getStringProperty(String key) {
        //TODO fill this method
       return null;
    }
}
