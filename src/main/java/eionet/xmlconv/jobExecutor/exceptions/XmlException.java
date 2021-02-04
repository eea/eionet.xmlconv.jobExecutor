package eionet.xmlconv.jobExecutor.exceptions;

public class XmlException extends Exception {
    /**
     * Default constructor
     */
    public XmlException() {
    }

    /**
     * Constructor
     * @param msg Message
     */
    public XmlException(String msg) {
        super(msg);
    }

    /**
     * Constructor
     * @param e Exception
     */
    public XmlException(Exception e) {
        super(e);
    }
}
