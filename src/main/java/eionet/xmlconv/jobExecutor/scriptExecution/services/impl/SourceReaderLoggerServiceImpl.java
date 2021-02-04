package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.models.ConversionLogDto;
import eionet.xmlconv.jobExecutor.models.ConversionResultDto;
import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderLoggerService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SourceReaderLoggerServiceImpl implements SourceReaderLoggerService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceReaderLoggerService.class);

    private long startTimestamp = 0;

    /**
     * Enum storing reader type messages.
     */
    public enum ReaderTypeEnum {
        EXCEL("MS Excel"), ODS("OpenOffice Spreadsheet");
        private String message;

        /**
         * Constructor
         * @param message Reader type
         */
        ReaderTypeEnum(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Conversion result object where to write log messages.
     */
    private ConversionResultDto conversionResult;
    /**
     * Reader type indicating whether it is Excel or OpenDocument conversion
     */
    private ReaderTypeEnum readerType;

    @Autowired
    public SourceReaderLoggerServiceImpl(){}

    /**
     * Sets conversion result and reader type
     * @param conversionResult Conversion result
     * @param readerType Reader type
     */
    public SourceReaderLoggerServiceImpl(ConversionResultDto conversionResult, ReaderTypeEnum readerType) {
        this.conversionResult = conversionResult;
        this.readerType = readerType;
    }

    /**
     * Start reading spreadsheet.
     */
    @Override
    public void logStartWorkbook() {
        startTimestamp = System.currentTimeMillis();
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_START_SPREADSHEET), ConversionLogDto.CATEGORY_WORKBOOK);
    }

    /**
     * Could not find any table definitions from Data Dictionary for given MS Excel file
     */
    @Override
    public void logNoDefinitionsForTables() {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.ERROR,
                Properties.getMessage(Constants.CONVERSION_LOG_NO_DEFINITIONS, new String[] {readerType.getMessage()}),
                ConversionLogDto.CATEGORY_WORKBOOK);
    }

    /**
     * Found " + numberOfSheets + " sheets from the workbook
     *
     * @param numberOfSheets Number of workbook sheets
     * @param sheetNames Sheet names
     */
    @Override
    public void logNumberOfSheets(int numberOfSheets, String sheetNames) {
        String plural = (numberOfSheets != 1) ? "s" : "";
        conversionResult.addConversionLog(
                ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_NOF_SHEETS, new String[] {Integer.toString(numberOfSheets),
                        sheetNames, plural}), ConversionLogDto.CATEGORY_WORKBOOK);
    }

    /**
     * Start reading sheet:
     *
     * @param sheetName Sheet name
     */
    @Override
    public void logStartSheet(String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_START_SHEET, new String[] {sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * Unable to find sheet
     *
     * @param sheetName Sheet name
     */
    @Override
    public void logSheetNotFound(String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.WARNING,
                Properties.getMessage(Constants.CONVERSION_LOG_NO_SHEET, new String[] {sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * Sheet is missing or empty. Skip converting this sheet:
     *
     * @param sheetName Sheet name
     */
    @Override
    public void logEmptySheet(String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_EMPTY_SHEET, new String[] {sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * "Found nofColumns columns on sheet: sheetName
     *
     * @param nofColumns Number of columns
     * @param sheetName Sheet name
     */
    @Override
    public void logNumberOfColumns(int nofColumns, String sheetName) {
        String plural = (nofColumns != 1) ? "s" : "";
        conversionResult.addConversionLog(
                ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_NOF_COLS, new String[] {Integer.toString(nofColumns),
                        sheetName, plural}), ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * Found redundant columns: extraColumns on sheet sheetName
     *
     * @param extraColumns Extra columns
     * @param sheetName Sheet name
     */
    @Override
    public void logExtraColumns(String extraColumns, String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.WARNING,
                Properties.getMessage(Constants.CONVERSION_LOG_REDUNDANT_COLS, new String[] {extraColumns, sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * The following columns are missing: missingColumns on sheet sheetName
     *
     * @param missingColumns Missing columns
     * @param sheetName Sheet name
     */
    @Override
    public void logMissingColumns(String missingColumns, String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.WARNING,
                Properties.getMessage(Constants.CONVERSION_LOG_MISSING_COLS, new String[] {missingColumns, sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * End reading sheet:
     *
     * @param sheetName Sheet name
     */
    @Override
    public void logEndSheet(String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_END_SHEET, new String[] {sheetName}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * Found numberOfRows records on sheet: sheetName
     *
     * @param numberOfRows Number of rows
     * @param sheetName Sheet name
     */
    @Override
    public void logNumberOfRows(int numberOfRows, String sheetName) {
        String plural = (numberOfRows != 1) ? "s" : "";
        conversionResult.addConversionLog(
                ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_NOF_RECORDS, new String[] {Integer.toString(numberOfRows),
                        sheetName, plural}), ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * End reading spreadsheet.
     * @param fileSize File size
     */
    @Override
    public void logEndWorkbook(long fileSize) {
        BigDecimal totalTime = new BigDecimal(System.currentTimeMillis() - startTimestamp).divide(new BigDecimal("1000.0"));
        String fileSizeMessage = "";
        if (fileSize > 0) {
            fileSizeMessage = FileUtils.byteCountToDisplaySize(fileSize);
        }
        String message =
                Properties.getMessage(Constants.CONVERSION_LOG_END_SPREADSHEET, new String[] {fileSizeMessage, totalTime.toPlainString()});
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO, message, ConversionLogDto.CATEGORY_WORKBOOK);
        LOGGER.info(message);
    }

    /**
     * Sheet schema is: schemaUrl
     *
     * @param instanceUrl Instance URL
     * @param sheetName Table local name
     */
    @Override
    public void logSheetSchema(String instanceUrl, String sheetName) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                Properties.getMessage(Constants.CONVERSION_LOG_SHEET_SCHEMA, new String[] {instanceUrl}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }
    /**
     * Log system warning:
     *
     * @param sheetName Sheet name
     * @param warnMessage Warning message
     */
    @Override
    public void logSystemWarning(String sheetName, String warnMessage) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.WARNING,
                Properties.getMessage(Constants.CONVERSION_LOG_WARNING, new String[] {sheetName, warnMessage}),
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }

    /**
     * Log system info:
     *
     * @param sheetName Sheet name
     * @param infoMessage Information message
     */
    @Override
    public void logInfo(String sheetName, String infoMessage) {
        conversionResult.addConversionLog(ConversionLogDto.ConversionLogType.INFO,
                infoMessage,
                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
    }
}
