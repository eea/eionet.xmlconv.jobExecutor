package eionet.xmlconv.jobExecutor.utils.tiny;

import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.TinyTreeXpathServiceImpl;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TinyTreeContext {

    protected XdmNode document = null;
    /** Saxon Tiny Tree DOM wrapper **/
    private DocumentBuilder parser;
    private Processor proc;

    /**
     * Tiny tree Parser constructor
     */
    public TinyTreeContext() {
        proc = new Processor(false);
        parser = proc.newDocumentBuilder();
    }

    /**
     * Parses file into tiny tree object.
     * @param fileName - File path
     */
    public void setFile(String fileName) {
        File file = new File(fileName);
        try {
            document = parser.build(file);
        } catch (SaxonApiException e) {
            try {
                throw new XmlException("Failed to parse file.");
            } catch (XmlException e1) {
                // do nothing
            }
        }
    }

    /**
     * Parses inputstream into tiny tree object.
     * @param inputStream InputStream to parse.
     */
    public void setStream(InputStream inputStream) {
        StreamSource source = new StreamSource(inputStream);
        try {
            document = parser.build(source);
        } catch (SaxonApiException e) {
            try {
                throw new XmlException("Failed to parse file.");
            } catch (XmlException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public TinyTreeXpathServiceImpl getQueryManager() {
        return new TinyTreeXpathServiceImpl(proc, document);
    }
}
