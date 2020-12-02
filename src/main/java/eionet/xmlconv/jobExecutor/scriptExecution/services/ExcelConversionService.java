package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;

import java.io.OutputStream;

public interface ExcelConversionService {
    /**
     * Sets the filename to output file
     *
     * @param name
     *            - MS Excel file name (full path)
     */
    void setFileName(String name);

    /**
     * Adds a new worksheet into workbook
     *
     * @param sheetName
     *            - name of the new worksheet
     */
    void addWorksheets(String sheetName);

    /**
     * Adds a new row to the active worksheet
     *
     * @param def_style
     *            - default style name
     * @param def_type
     *            - default data type
     */
    void addRow(String def_style, String def_type);

    /**
     * Adds several new rows to the active worksheet
     *
     * @param def_style
     *            - default style name
     * @param def_type
     *            - default data type
     * @param repeated
     *            - the number of new rows
     */
    void addRows(String def_style, String def_type, int repeated);

    /**
     * Adds a new column to the active worksheet
     *
     * @param def_style
     *            - default style name
     * @param def_type
     *            - default data type
     */
    void addColumn(String def_style, String def_type);

    /**
     * Adds several new columns to the active worksheet
     *
     * @param def_style
     *            - default style name
     * @param def_type
     *            - default data type
     * @param repeated
     *            - the number of new rows
     */
    void addColumns(String def_style, String def_type, int repeated);

    /**
     * Adds a new cell to the active worksheet and active row
     *
     * @param type
     *            - data type for the new cell, if not defined, then inherited from parent level
     * @param str_value
     *            - cell value
     * @param style_name
     *            - style name
     */
    void addCell(String type, String str_value, String style_name);

    /**
     * Adds several empty cells to the active worksheet and active row
     *
     * @param type
     *            - data type for the new cell, if not defined, then inherited from parent level
     * @param style_name
     *            - style name
     * @param repeated
     *            - the number of new cells
     */
    void addCells(String type, String style_name, int repeated);

    /**
     * Adds a new Excel style to the active workbook
     *
     * @param style
     *            - predefined excel style
     */
    void addStyle(ExcelStyleService style);

    /**
     * Returns the excel style by style name
     *
     * @param name
     *            - Excel style name
     * @param family
     *            - Excel objects family (sheet, row, column, cell)
     * @return excel style object
     */
    ExcelStyleService getStyleByName(String name, String family);

    /**
     * Writes the EXCEL workbook object into file
     * @throws ConversionException In case an error occurs.
     */
    void writeToFile() throws ConversionException;

    /**
     * Writes the EXCEL workbook object into output stream
     * @param outstream OutputStream
     * @throws ConversionException In case an error occurs.
     */
    void writeToFile(OutputStream outstream) throws ConversionException;
}
