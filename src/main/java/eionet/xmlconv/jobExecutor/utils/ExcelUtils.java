package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.scriptExecution.services.ExcelConversionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ExcelStyleService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ExcelConversionServiceImpl;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ExcelStyleServiceImpl;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.readers.ExcelReaderServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;

/**
 * Excel conversion utilities.
 */
public class ExcelUtils {

    /**
     * Returns a valid ExcelConversionHandlerIF
     * @return Excel conversion handler
     */
    public static ExcelConversionService getExcelConversionHandler() {
        return new ExcelConversionServiceImpl();
    }

    /**
     * Returns a valid ExcelStyleIF
     * @return  Excel style
     */
    public static ExcelStyleService getExcelStyle() {
        return new ExcelStyleServiceImpl();
    }

    /**
     * Returns a valid ExcelReaderIF
     * @return Excel reader
     */
    public static SourceReaderService getExcelReader() {
        return new ExcelReaderServiceImpl(false);
    }

    /**
     * Returns a valid ExcelReaderIF
     * @return Excel2007 reader
     */
    public static SourceReaderService getExcel2007Reader() {
        return new ExcelReaderServiceImpl(true);
    }

    /**
     * Returns true, if InputStream can be opened with MS Excel.
     * @param input InputStream
     * @return True, if InputStream can be opened with MS Excel.
     */
    public static boolean isExcelFile(InputStream input) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(input);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Determines if stream is Excel 2007 file.
     * @param input InputStream
     * @return True if InputStream is Excel 2007 file.
     */
    public static boolean isExcel2007File(InputStream input) {
        try {
            OPCPackage p = OPCPackage.open(input);
            Workbook wb = WorkbookFactory.create(p);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
