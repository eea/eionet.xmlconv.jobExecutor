package eionet.xmlconv.jobExecutor.scriptExecution.processors;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ExcelConversionService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ExcelXMLServiceImpl;
import eionet.xmlconv.jobExecutor.utils.ExcelUtils;
import org.apache.commons.io.IOUtils;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ExcelProcessor  {
    /**
     * Default constructor.
     */
    public ExcelProcessor() {
    }

    /**
     * Converts XML string to Excel
     * @param sIn Input string
     * @param sOut Output string
     * @throws ConversionException In case an error occurs.
     */
    public void makeExcel(String sIn, String sOut) throws ConversionException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(sOut);
            makeExcel(sIn, outStream);
        } catch (Exception e) {
            throw new ConversionException("ErrorConversionHandler - couldn't save the Excel file: " + e.toString(), e);
        } finally {
            IOUtils.closeQuietly(outStream);
        }
    }

    /**
     * Converts XML string to OutputStream
     * @param sIn Input string
     * @param sOut OutputStream
     * @throws ConversionException In case an error occurs.
     */
    public void makeExcel(String sIn, OutputStream sOut) throws ConversionException {

        if (sIn == null) {
            return;
        }
        if (sOut == null) {
            return;
        }

        try {
            ExcelConversionService excel = ExcelUtils.getExcelConversionHandler();
            //excel.setFileName(sOut);

            ExcelXMLServiceImpl handler = new ExcelXMLServiceImpl(excel);
            SAXParserFactory spfact = SAXParserFactory.newInstance();
            SAXParser parser = spfact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            spfact.setValidating(true);

            reader.setContentHandler(handler);
            reader.parse(sIn);
            excel.writeToFile(sOut);
        } catch (Exception e) {
            throw new ConversionException("Error generating Excel file: " + e.toString(), e);
        }

        return;
    }
}
