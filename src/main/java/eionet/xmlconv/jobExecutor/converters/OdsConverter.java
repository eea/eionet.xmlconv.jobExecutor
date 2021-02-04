package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.scriptExecution.processors.OpenDocumentProcessor;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OdsConverter extends ConvertStrategy {
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(OdsConverter.class);

    @Override
    public String convert(InputStream source, InputStream xslt, OutputStream result, String cnvFileExt) throws ConversionException,
            Exception {
        FileOutputStream xmlOut = null;
        String xmlFile =  Utils.getUniqueTmpFileName(".xml");
        String odsFile =  Utils.getUniqueTmpFileName(".ods");

        try {
            xmlOut = new FileOutputStream(xmlFile);
            runXslTransformation(source, xslt, xmlOut);
            OpenDocumentProcessor odp = new OpenDocumentProcessor();
            if (result != null) {
                odp.makeSpreadsheet(xmlFile, result);
            } else {
                odp.makeSpreadsheet(xmlFile, odsFile);
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Error " + e.toString(), e);
            throw new ConversionException("Error transforming OpenDocument Spreadhseet " + e.toString(), e);
        } finally {
            IOUtils.closeQuietly(xmlOut);
        }
        try {
            Utils.deleteFile(xmlFile);
        } catch (Exception e) {
            LOGGER.error("Couldn't delete the result file: " + xmlFile, e);
        }

        return odsFile;
    }

}
