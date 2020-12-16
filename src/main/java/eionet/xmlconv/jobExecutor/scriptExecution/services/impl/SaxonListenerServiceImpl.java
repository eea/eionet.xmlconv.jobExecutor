package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import net.sf.saxon.lib.StandardErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.transform.TransformerException;

@Service
public class SaxonListenerServiceImpl extends StandardErrorListener {
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SaxonListenerServiceImpl.class);

    private StringBuilder _errBuf; // in this buffer we collect all the error messages
    private boolean _hasErrors = false;

    /**
     * Default constructor
     */
    @Autowired
    public SaxonListenerServiceImpl() {
        _errBuf = new StringBuilder();
    }

    /**
     * Returns if listener has errors
     * @return
     */
    boolean hasErrors() {
        return _hasErrors;
    }

    /**
     * Returns all the error messages gathered when processing the XQuery script
     *
     * @return String errors - all the errors
     */
    public String getErrors() {
        return _errBuf.toString();
    }

    @Override
    public void error(TransformerException exception) {
        _hasErrors = true;
        String message = "Error " + getLocationMessage(exception) + "\n  " + getExpandedMessage(exception);

        _errBuf.append(message).append("\n");
        super.error(exception);
    }

    @Override
    public void warning(TransformerException exception) {
        _hasErrors = true;
        String message = "";
        if (exception.getLocator() != null) {
            message = getLocationMessage(exception) + "\n  ";
        }
        message += getExpandedMessage(exception);

        _errBuf.append(message).append("\n");

        super.warning(exception);
    }
}
