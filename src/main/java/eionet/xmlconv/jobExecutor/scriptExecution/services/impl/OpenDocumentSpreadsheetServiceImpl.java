package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.OpenDocumentSpreadsheetService;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenDocumentSpreadsheetServiceImpl implements OpenDocumentSpreadsheetService {

    private String currentTable = null;

    private List<String> tables;

    private Map<String, List<List<String>>> tablesData;

    private Map<String, List<String>> tablesHeaders;

    /**
     * Default Constructor.
     */
    @Autowired
    public OpenDocumentSpreadsheetServiceImpl() {
        this.tables = new ArrayList<String>();
        this.tablesData = new HashMap<String, List<List<String>>>();
        this.tablesHeaders = new HashMap<String, List<String>>();
    }

    public void setCurrentTable(String currentTable) {
        this.currentTable = currentTable;
    }

    public String getCurrentTable() {
        return this.currentTable;
    }

    /**
     * Adds table name to the tables vector
     * @param tbl_name Table name
     */
    @Override
    public void addTable(String tbl_name) {
        tables.add(tbl_name);
        setCurrentTable(tbl_name);
    }

    /**
     * Returns tables' list
     */
    @Override
    public List<String> getTables() {
        return tables;
    }

    /**
     * Returns table name in specified index
     * @param idx Index
     */
    @Override
    public String getTableName(int idx) {
        if (idx <= tables.size()) {
            return tables.get(idx);
        } else {
            return null;
        }
    }

    /**
     * Adds header list into headers Map
     * @param tbl_name Table name
     * @param value Value
     */
    @Override
    public void addTableHeaderValue(String tbl_name, String value) {
        if (Utils.isNullStr(tbl_name)) {
            tbl_name = currentTable;
        }
        List<String> list = null;

        if (tablesHeaders.containsKey(tbl_name)) {
            list = tablesHeaders.get(tbl_name);
            list.add(value);
        } else {
            list = new ArrayList<String>();
            list.add(value);
            tablesHeaders.put(tbl_name, list);
        }

    }

    /**
     * Adds data row into data Map
     * @param tbl_name Table name
     * @param row_list Row list
     */
    @Override
    public void addTableDataRow(String tbl_name, List<String> row_list) {
        if (Utils.isNullStr(tbl_name)) {
            tbl_name = currentTable;
        }
        List<List<String>> rows_list = null;

        if (tablesData.containsKey(tbl_name)) {
            rows_list = tablesData.get(tbl_name);
            rows_list.add(row_list);
        } else {
            rows_list = new ArrayList<List<String>>();
            rows_list.add(row_list);
            tablesData.put(tbl_name, rows_list);
        }
    }

    /**
     * Returns tables' data as ArrayList
     * @param tblName Table name
     */
    @Override
    public List<List<String>> getTableData(String tblName) {
        if (Utils.isNullStr(tblName)) {
            tblName = currentTable;
        }

        List<List<String>> list = null;
        if (tablesData.containsKey(tblName)) {
            list = tablesData.get(tblName);
        }

        return list;
    }

    /**
     * Gets table header list
     * @param tbl_name Table name
     */
    @Override
    public List<String> getTableHeader(String tbl_name) {
        if (Utils.isNullStr(tbl_name)) {
            tbl_name = currentTable;
        }

        List<String> list = null;
        if (tablesHeaders.containsKey(tbl_name)) {
            list = tablesHeaders.get(tbl_name);
        }

        return list;
    }

    /**
     * Gets table header list
     * @param tbl_name Table name
     */
    @Override
    public int getTableColCount(String tbl_name) {
        if (Utils.isNullStr(tbl_name)) {
            tbl_name = currentTable;
        }

        int i = 0;
        if (tablesHeaders == null) {
            return i;
        }

        if (tablesHeaders.containsKey(tbl_name)) {
            try {
                if (tablesHeaders.get(tbl_name) != null) {
                    i = tablesHeaders.get(tbl_name).size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return i;
    }

    /**
     * Gets table row count
     * @param tbl_name Table name
     */
    @Override
    public int getTableRowCount(String tbl_name) {

        int i = 0;

        if (Utils.isNullStr(tbl_name)) {
            tbl_name = currentTable;
        }
        if (tablesData == null) {
            return i;
        }

        if (tablesData.containsKey(tbl_name)) {
            try {
                if (tablesData.get(tbl_name) != null) {
                    i = tablesData.get(tbl_name).size();
                }
            } catch (Exception e) {
                // Todo fix logger
                // do nothing return 0
                e.printStackTrace();
            }
        }

        return i;
    }

    /**
     * Checks if table exists
     * @param tblName Table name
     */
    @Override
    public boolean tableExists(String tblName) {
        if (tables == null) {
            return false;
        }
        return tables.contains(tblName);

    }

    /**
     * Checks if sheet contains any data.
     * @param tblName Table name
     */
    @Override
    public boolean isEmptySheet(String tblName) {

        if (Utils.isNullStr(tblName)) {
            tblName = currentTable;
        }
        // data does not exist
        if (tablesData == null) {
            return true;
        }

        // Table does not exist
        if (!tablesData.containsKey(tblName)) {
            return true;
        }

        try {
            List<List<String>> rows = tablesData.get(tblName);

            // no data rows
            if (rows == null || rows.size() == 0) {
                return true;
            }
            for (int n = 0; n < rows.size(); n++) {
                try {
                    List<String> row = rows.get(n);

                    // If row contains any String data, then it is not empty,
                    // return false
                    if (!Utils.isEmptyList(row)) {
                        return false;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            return true;
        }

        return true;

    }
}
