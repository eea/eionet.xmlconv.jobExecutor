package eionet.xmlconv.jobExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:message.properties")
public class Constants {

    /**
     * Private constructor to deal with reflection.
     */
    public Constants() {
    }

    public static final int XQ_WORKER_RECEIVED = 0;
    public static final int XQ_WORKER_SUCCESS = 1;
    public static final int XQ_WORKER_FATAL_ERR = 2;
    public static final int XQ_WORKER_INTERRUPTED = 3;

    public static final String WARNING_QA_EXPIRED_DD_SCHEMA = "The reported XML file uses an obsolete version of Data Dictionary XML Schema. The last version of given dataset is released on {0} with ID={1}.";
    public static final String GETSOURCE_URL = "/s/getsource";
    public static final String SOURCE_URL_PARAM = "source_url";
    public static final String TICKET_PARAM = "ticket";
    public static final String TMP_FILE_PREFIX = "xmlconv_tmp_";
    /**
     * Default parameter name of the source URL to be given to the XQuery script by the QA service
     */
    public static final String XQ_SOURCE_PARAM_NAME = "source_url";

    public static String CONVERSION_LOG_START_SPREADSHEET;
    public static String CONVERSION_LOG_NO_DEFINITIONS;
    public static String CONVERSION_LOG_NOF_SHEETS;
    public static String CONVERSION_LOG_START_SHEET;
    public static String CONVERSION_LOG_NO_SHEET;
    public static String CONVERSION_LOG_EMPTY_SHEET;
    public static String CONVERSION_LOG_NOF_COLS;
    public static String CONVERSION_LOG_REDUNDANT_COLS;
    public static String CONVERSION_LOG_MISSING_COLS;
    public static String CONVERSION_LOG_END_SHEET;
    public static String CONVERSION_LOG_NOF_RECORDS;
    public static String CONVERSION_LOG_END_SPREADSHEET;
    public static String CONVERSION_LOG_SHEET_SCHEMA;
    public static String CONVERSION_LOG_WARNING;
    public static String ERROR_CONVERSION_OBSOLETE_TEMPLATE;
    public static String ERROR_CONVERSION_INVALID_TEMPLATE;
    public static String WORKER_LOG_JOB_RECEIVED;
    public static String WORKER_LOG_JOB_SUCCESS;
    public static String WORKER_LOG_JOB_FAILURE;
    public static String WARNING_QA_EXPIRED_SCHEMA;

    @Value( "${conversion.log.start.spreadsheet}" )
    public void setConversionLogStartSpreadsheet(String conversionLogStartSpreadsheet) {
        this.CONVERSION_LOG_START_SPREADSHEET = conversionLogStartSpreadsheet;
    }

    @Value( "${conversion.log.no.definitions}" )
    public void setConversionLogNoDefinitions(String conversionLogNoDefinitions) {
        CONVERSION_LOG_NO_DEFINITIONS = conversionLogNoDefinitions;
    }

    @Value( "${conversion.log.nof.sheets}" )
    public void setConversionLogNofSheets(String conversionLogNofSheets) {
        CONVERSION_LOG_NOF_SHEETS = conversionLogNofSheets;
    }

    @Value( "${conversion.log.start.sheet}" )
    public void setConversionLogStartSheet(String conversionLogStartSheet) {
        CONVERSION_LOG_START_SHEET = conversionLogStartSheet;
    }

    @Value( "${conversion.log.no.sheet}" )
    public void setConversionLogNoSheet(String conversionLogNoSheet) {
        CONVERSION_LOG_NO_SHEET = conversionLogNoSheet;
    }

    @Value( "${conversion.log.empty.sheet}" )
    public void setConversionLogEmptySheet(String conversionLogEmptySheet) {
        CONVERSION_LOG_EMPTY_SHEET = conversionLogEmptySheet;
    }

    @Value( "${conversion.log.nof.cols}" )
    public void setConversionLogNofCols(String conversionLogNofCols) {
        CONVERSION_LOG_NOF_COLS = conversionLogNofCols;
    }

    @Value( "${conversion.log.redundant.cols}" )
    public void setConversionLogRedundantCols(String conversionLogRedundantCols) {
        CONVERSION_LOG_REDUNDANT_COLS = conversionLogRedundantCols;
    }

    @Value( "${conversion.log.missing.cols}" )
    public void setConversionLogMissingCols(String conversionLogMissingCols) {
        CONVERSION_LOG_MISSING_COLS = conversionLogMissingCols;
    }

    @Value( "${conversion.log.end.sheet}" )
    public void setConversionLogEndSheet(String conversionLogEndSheet) {
        CONVERSION_LOG_END_SHEET = conversionLogEndSheet;
    }

    @Value( "${conversion.log.nof.records}" )
    public void setConversionLogNofRecords(String conversionLogNofRecords) {
        CONVERSION_LOG_NOF_RECORDS = conversionLogNofRecords;
    }

    @Value( "${conversion.log.end.spreadsheet}" )
    public void setConversionLogEndSpreadsheet(String conversionLogEndSpreadsheet) {
        CONVERSION_LOG_END_SPREADSHEET = conversionLogEndSpreadsheet;
    }

    @Value( "${conversion.log.sheet.schema}" )
    public void setConversionLogSheetSchema(String conversionLogSheetSchema) {
        CONVERSION_LOG_SHEET_SCHEMA = conversionLogSheetSchema;
    }

    @Value( "${conversion.log.warning}" )
    public void setConversionLogWarning(String conversionLogWarning) {
        CONVERSION_LOG_WARNING = conversionLogWarning;
    }

    @Value( "${error.conversion.obsolete.template}" )
    public void setErrorConversionObsoleteTemplate(String errorConversionObsoleteTemplate) {
        ERROR_CONVERSION_OBSOLETE_TEMPLATE = errorConversionObsoleteTemplate;
    }

    @Value( "${error.conversion.invalid.template}" )
    public void setErrorConversionInvalidTemplate(String errorConversionInvalidTemplate) {
        ERROR_CONVERSION_INVALID_TEMPLATE = errorConversionInvalidTemplate;
    }

    @Value( "${worker.log.job.received}" )
    public void setWorkerLogJobReceived(String workerLogJobReceived) {
        WORKER_LOG_JOB_RECEIVED = workerLogJobReceived;
    }

    @Value( "${worker.log.job.success}" )
    public void setWorkerLogJobSuccess(String workerLogJobSuccess) {
        WORKER_LOG_JOB_SUCCESS = workerLogJobSuccess;
    }

    @Value( "${worker.log.job.failure}" )
    public void setWorkerLogJobFailure(String workerLogJobFailure) {
        WORKER_LOG_JOB_FAILURE = workerLogJobFailure;
    }

    @Value( "${warning.qa.expired.schema}" )
    public void setWarningQaExpiredSchema(String warningQaExpiredSchema) {
        WARNING_QA_EXPIRED_SCHEMA = warningQaExpiredSchema;
    }
}
