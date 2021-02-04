package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.ExcelStyleService;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class is mapping styles between xml (fo:style) and sytles defined in poi HSSF.
 *
 */
@Service
public class ExcelStyleServiceImpl implements ExcelStyleService {

    private String name = null;
    private String family = null;
    private boolean italic = false;
    private short font_weight = HSSFFont.BOLDWEIGHT_NORMAL;
    private short font_size = 12;
    private String font_name = null;
    private short text_align = HSSFCellStyle.ALIGN_GENERAL;
    private short workbook_index = -1;
    // width - - the width in units of 1/256th of a character width
    private short column_width = 0;

    /**
     * Default constructor
     */
    @Autowired
    public ExcelStyleServiceImpl() {
    }

    @Override
    public void setExcelStyle(String name, String family) {
        // These are style unique id's
        this.name = name;
        this.family = family;
    }

    // get methods
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFamily() {
        return family;
    }

    @Override
    public boolean getItalic() {
        return italic;
    }

    @Override
    public short getFontWeight() {
        return font_weight;
    }

    @Override
    public short getFontSize() {
        return font_size;
    }

    @Override
    public String getFontName() {
        return font_name;
    }

    @Override
    public short getTextAlign() {
        return text_align;
    }

    @Override
    public short getWorkbookIndex() {
        return workbook_index;
    }

    // set methods
    @Override
    public void setItalic(String str_italic) {
        if (str_italic == null) {
            return;
        }
        if (str_italic.equalsIgnoreCase("italic")) {
            italic = true;
        }
    }

    @Override
    public void setFontWeight(String str_bold) {
        if (str_bold == null) {
            return;
        }
        if (str_bold.equalsIgnoreCase("bold")) {
            font_weight = HSSFFont.BOLDWEIGHT_BOLD;
        }
    }

    @Override
    public void setFontSize(String str_size) {
        Short short_size = null;
        if (str_size == null) {
            return;
        }
        if (str_size.endsWith("pt")) {
            str_size = str_size.substring(0, str_size.indexOf("pt"));
        }
        try {
            short_size = new Short(str_size);
        } catch (Exception e) {
            return;
        }
        if (short_size != null) {
            font_size = short_size.shortValue();
        }
    }

    @Override
    public void setFontName(String str_fontname) {
        if (str_fontname == null) {
            return;
        }
        this.font_name = str_fontname;
    }

    @Override
    public void setTextAlgin(String str_align) {
        if (str_align == null) {
            return;
        }
        if (str_align.equalsIgnoreCase("center")) {
            text_align = HSSFCellStyle.ALIGN_CENTER;
        } else if (str_align.equalsIgnoreCase("left")) {
            text_align = HSSFCellStyle.ALIGN_LEFT;
        } else if (str_align.equalsIgnoreCase("right") || str_align.equalsIgnoreCase("end")) {
            text_align = HSSFCellStyle.ALIGN_RIGHT;
        } else if (str_align.equalsIgnoreCase("justify")) {
            text_align = HSSFCellStyle.ALIGN_JUSTIFY;
        } else if (str_align.equalsIgnoreCase("left")) {
            text_align = HSSFCellStyle.ALIGN_LEFT;
        } else if (str_align.equalsIgnoreCase("start")) {
            text_align = HSSFCellStyle.ALIGN_GENERAL;
        } else {
            text_align = HSSFCellStyle.ALIGN_GENERAL;
        }
    }

    @Override
    public boolean equals(ExcelStyleService style) {

        String compare_name = style.getName();
        String compare_family = style.getFamily();

        if (compare_name == null || compare_family == null) {
            return false;
        }

        if (compare_name.equalsIgnoreCase(this.name) && compare_family.equalsIgnoreCase(this.family)) {
            return true;
        }

        return false;
    }

    @Override
    public void setWorkbookIndex(short index) {
        this.workbook_index = index;
    }

    @Override
    public short getColumnWidth() {
        return column_width;
    }

    // xcel bases its measurement of column widths on the number of digits
    // (specifically, the number of zeros) in the column, using the Normal style font.
    // (There are some fonts that have digits of different widths, but this is unusual.)

    // For example, using the default font, a column with a width of 10 refers to the column
    // width needed to display 10 non-bold, non-italic, Arial 10-point zeros.

    // POI waits the column width set in units of 1/256th of a character width
    @Override
    public void setColumnWidth(String str_column_width) {
        Short numcolumn_width = null;
        if (str_column_width == null) {
            return;
        }
        int x = 256;

        if (str_column_width.endsWith("cm")) {
            str_column_width = str_column_width.substring(0, str_column_width.indexOf("cm"));
            x = 1280;
        }
        try {
            float l = Float.parseFloat(str_column_width);
            float full_width = l * x;
            numcolumn_width = Short.parseShort(String.valueOf(Math.round(full_width)));
        } catch (Exception e) {
            return;
        }
        if (numcolumn_width != null) {
            column_width = numcolumn_width.shortValue();
        }
    }
}
