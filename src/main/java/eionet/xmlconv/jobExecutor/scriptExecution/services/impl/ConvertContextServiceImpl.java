package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.converters.ConvertStrategy;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ConvertContextService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class ConvertContextServiceImpl implements ConvertContextService {

    private InputStream source;
    private String xslName;
    private InputStream xslStream;
    private OutputStream resultStream;
    private String cnvFileExt;

    @Autowired
    public ConvertContextServiceImpl() {
    }

    /**
     * Constructor.
     * @param source Source
     * @param xslName Xsl name
     * @param result Result OutputStream
     * @param cnvFileExt File extension
     */
    public ConvertContextServiceImpl(InputStream source, String xslName, OutputStream result, String cnvFileExt) {
        this.cnvFileExt = cnvFileExt;
        this.resultStream = result;
        this.source = source;
        this.xslName = xslName;
        this.xslStream = null;
    }

    /**
     * Constructor.
     * @param source Source
     * @param xslStream Xsl stream
     * @param result Result OutputStream
     * @param cnvFileExt File extension
     */
    public ConvertContextServiceImpl(InputStream source, InputStream xslStream, OutputStream result, String cnvFileExt) {
        this.cnvFileExt = cnvFileExt;
        this.resultStream = result;
        this.source = source;
        this.xslName = null;
        this.xslStream = xslStream;
    }

    /**
     * Executes Conversion.
     * @param converter Converter
     * @return Result
     * @throws Exception If conversion can't be completed
     */
    @Override
    public String executeConversion(ConvertStrategy converter) throws Exception {
        String strResult = null;

        if (xslStream == null) {
            xslStream = new BufferedInputStream(new FileInputStream(xslName));
        }
        if (xslName != null) {
            converter.setXslPath(xslName);
        }
        try {
            strResult = converter.convert(source, xslStream, resultStream, cnvFileExt);
        } finally {
            IOUtils.closeQuietly(xslStream);
        }
        return strResult;
    }

}
