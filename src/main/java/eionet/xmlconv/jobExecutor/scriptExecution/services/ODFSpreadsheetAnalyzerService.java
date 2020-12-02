package eionet.xmlconv.jobExecutor.scriptExecution.services;

import java.io.InputStream;

public interface ODFSpreadsheetAnalyzerService {
    OpenDocumentSpreadsheetService analyzeSpreadsheet(InputStream metaStream);
    OpenDocumentSpreadsheetService analyzeZip(InputStream inputStream);
}
