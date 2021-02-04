package eionet.xmlconv.jobExecutor.scriptExecution.services;


public interface SourceReaderLoggerService {
    void logStartWorkbook();
    void logNoDefinitionsForTables();
    void logNumberOfSheets(int numberOfSheets, String sheetNames);
    void logStartSheet(String sheetName);
    void logSheetNotFound(String sheetName);
    void logEmptySheet(String sheetName);
    void logNumberOfColumns(int nofColumns, String sheetName);
    void logExtraColumns(String extraColumns, String sheetName);
    void logMissingColumns(String missingColumns, String sheetName);
    void logEndSheet(String sheetName);
    void logNumberOfRows(int numberOfRows, String sheetName);
    void logEndWorkbook(long fileSize);
    void logSheetSchema(String instanceUrl, String sheetName);
    void logSystemWarning(String sheetName, String warnMessage);
    void logInfo(String sheetName, String infoMessage);

}
