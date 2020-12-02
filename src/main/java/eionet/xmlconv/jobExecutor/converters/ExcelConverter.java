package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.scriptExecution.processors.ExcelProcessor;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ExcelConverter extends ConvertStrategy {
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelConverter.class);

    @Override
    public String convert(InputStream source, InputStream xslt, OutputStream result, String cnvFileExt) throws ConversionException,
            Exception {
        String xmlFile = Utils.getUniqueTmpFileName(".xml");
        String excelFile = Utils.getUniqueTmpFileName(".xls");
        OutputStream xmlOut = null;
        try {
            xmlOut = new FileOutputStream(xmlFile);
            runXslTransformation(source, xslt, xmlOut);
            ExcelProcessor ep = new ExcelProcessor();
            if (result != null) {
                ep.makeExcel(xmlFile, result);
            } else {
                ep.makeExcel(xmlFile, excelFile);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error " + e.toString(), e);
            throw new ConversionException("Error transforming Excel " + e.toString(), e);
        }
        finally{
            IOUtils.closeQuietly(xmlOut);
        }
        try {
            Utils.deleteFile(xmlFile);
        } catch (Exception e) {
            LOGGER.error("Couldn't delete the result file: " + xmlFile, e);
        }
        return excelFile;
    }

}
