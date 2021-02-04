package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HTMLConverter extends ConvertStrategy {

    @Override
    public String convert(InputStream source, InputStream xslt, OutputStream result, String cnvFileExt) throws ConversionException,
            Exception {
        String htmlFile = Utils.getUniqueTmpFileName(".html");
        if (result != null) {
            runXslTransformation(source, xslt, result);
        } else {
            try {
                result = new FileOutputStream(htmlFile);
                runXslTransformation(source, xslt, result);
            } catch (IOException e) {
                throw new ConversionException("Error creating HTML output file " + e.toString(), e);
            }
            finally{
                IOUtils.closeQuietly(result);
            }
        }
        return htmlFile;
    }

}
