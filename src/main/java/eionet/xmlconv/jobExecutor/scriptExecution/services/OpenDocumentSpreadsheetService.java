package eionet.xmlconv.jobExecutor.scriptExecution.services;

import java.util.List;

public interface OpenDocumentSpreadsheetService {
    String getTableName(int idx);
    void addTableHeaderValue(String tbl_name, String value);
    void addTableDataRow(String tbl_name, List<String> row_list);
    List<List<String>> getTableData(String tblName);
    List<String> getTableHeader(String tbl_name);
    int getTableColCount(String tbl_name);
    int getTableRowCount(String tbl_name);
    boolean tableExists(String tblName);
    boolean isEmptySheet(String tblName);
    void addTable(String tbl_name);
    List<String> getTables();

}
