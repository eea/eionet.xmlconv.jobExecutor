package eionet.xmlconv.jobExecutor.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;

public class TransformerErrorListener implements javax.xml.transform.ErrorListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerErrorListener.class);

    @Override
    public void warning(TransformerException te) throws TransformerException {
        LOGGER.error(formatTransformerException(0, te));
    }

    @Override
    public void error(TransformerException te) throws TransformerException {
        throw new TransformerException(formatTransformerException(1, te));
    }

    @Override
    public void fatalError(TransformerException te) throws TransformerException {
        Throwable cause = te.getException();
        if (cause != null) {
            if (cause instanceof SAXException) {
                throw te;
            } else {
                throw new TransformerException(formatTransformerException(2, te));
            }
        } else {
            throw new TransformerException(formatTransformerException(2, te));
        }
    }

    /**
     * Formats transformer exceptions.
     * @param errType The error type
     * @param te The transformer exception
     * @return Formatted error message
     */
    public static String formatTransformerException(int errType, TransformerException te) {
        String[] errorTypes = {"WARNING", "ERROR", "FATAL ERROR"};
        String msg = te.getMessageAndLocation();
        String msgout = "The XSLT processor reported the following " + errorTypes[errType] + ":\n" + msg;
        return msgout;
    }

}
