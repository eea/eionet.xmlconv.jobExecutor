package eionet.xmlconv.jobExecutor.scriptExecution.processors;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.OpenDocumentService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.OpenDocumentServiceImpl;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class OpenDocumentProcessor {

    /**
     * This class is creating handlers for creating OpenDocument file from xml called from ConversionService
     */
    public OpenDocumentProcessor() {
    }

    /**
     * Creates ODS Spreadsheet
     * @param sIn Input String
     * @param sOut Output String
     * @throws ConversionException If an error occurs.
     */
    public void makeSpreadsheet(String sIn, String sOut) throws ConversionException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(sOut);
            makeSpreadsheet(sIn, out);
        } catch (Exception e) {
            throw new ConversionException("ErrorConversionHandler - couldn't save the OpenDocumentSpreadheet file: " + e.toString(), e);
        }
        finally{
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Creates ODS Spreadsheet
     * @param sIn Input String
     * @param sOut Output String
     * @throws ConversionException If an error occurs.
     */
    public void makeSpreadsheet(String sIn, OutputStream sOut) throws ConversionException {

        if (sIn == null) {
            return;
        }
        if (sOut == null) {
            return;
        }

        try {
            OpenDocumentService od = new OpenDocumentServiceImpl();
            od.setContentFile(sIn);
            od.createOdsFile(sOut);
        } catch (Exception e) {
            throw new ConversionException("Error generating OpenDocument Spreadsheet file: " + e.toString(), e);
        }

        return;
    }
}
