package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextConverter extends ConvertStrategy {

    @Override
    public String convert(InputStream source, InputStream xslt, OutputStream result, String cnvFileExt) throws ConversionException,
            Exception {
        String outFile =  Utils.getUniqueTmpFileName("." + cnvFileExt);
        if (result != null) {
            runXslTransformation(source, xslt, result);
        } else {
            try {
                result = new FileOutputStream(outFile);
                runXslTransformation(source, xslt, result);
            } catch (IOException e) {
                throw new ConversionException("Error creating TEXT output file:" + e.toString(), e);
            }
            finally{
                IOUtils.closeQuietly(result);
            }
        }
        return outFile;
    }

}