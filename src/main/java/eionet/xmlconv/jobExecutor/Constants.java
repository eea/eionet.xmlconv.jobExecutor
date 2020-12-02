package eionet.xmlconv.jobExecutor;

public final class Constants {

    /**
     * Private constructor to deal with reflection.
     */
    private Constants() {
        throw new AssertionError();
    }
    public static final String WARNING_QA_EXPIRED_SCHEMA = "warning.qa.expired.schema";
    public static final String WARNING_QA_EXPIRED_DD_SCHEMA = "The reported XML file uses an obsolete version of Data Dictionary XML Schema. The last version of given dataset is released on {0} with ID={1}.";
    public static final String GETSOURCE_URL = "/s/getsource";
    public static final String SOURCE_URL_PARAM = "source_url";
    public static final String TICKET_PARAM = "ticket";
    public static final String TMP_FILE_PREFIX = "xmlconv_tmp_";
    /**
     * Default parameter name of the source URL to be given to the XQuery script by the QA service
     */
    public static final String XQ_SOURCE_PARAM_NAME = "source_url";

    public static final String CONVERSION_LOG_START_SPREADSHEET = "conversion.log.start.spreadsheet";
    public static final String CONVERSION_LOG_NO_DEFINITIONS = "conversion.log.no.definitions";
    public static final String CONVERSION_LOG_NOF_SHEETS = "conversion.log.nof.sheets";
    public static final String CONVERSION_LOG_START_SHEET = "conversion.log.start.sheet";
    public static final String CONVERSION_LOG_NO_SHEET = "conversion.log.no.sheet";
    public static final String CONVERSION_LOG_EMPTY_SHEET = "conversion.log.empty.sheet";
    public static final String CONVERSION_LOG_NOF_COLS = "conversion.log.nof.cols";
    public static final String CONVERSION_LOG_REDUNDANT_COLS = "conversion.log.redundant.cols";
    public static final String CONVERSION_LOG_MISSING_COLS = "conversion.log.missing.cols";
    public static final String CONVERSION_LOG_END_SHEET = "conversion.log.end.sheet";
    public static final String CONVERSION_LOG_NOF_RECORDS = "conversion.log.nof.records";
    public static final String CONVERSION_LOG_END_SPREADSHEET = "conversion.log.end.spreadsheet";
    public static final String CONVERSION_LOG_SHEET_SCHEMA = "conversion.log.sheet.schema";
    public static final String CONVERSION_LOG_WARNING = "conversion.log.warning";

    public static final String ERROR_CONVERSION_OBSOLETE_TEMPLATE = "error.conversion.obsolete.template";
    public static final String ERROR_CONVERSION_INVALID_TEMPLATE = "error.conversion.invalid.template";
}
