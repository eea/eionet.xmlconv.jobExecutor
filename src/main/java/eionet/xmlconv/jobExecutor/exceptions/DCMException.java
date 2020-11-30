package eionet.xmlconv.jobExecutor.exceptions;

public class DCMException extends Exception {

    private String errorCode;

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Constructor
     *
     * @param errorCode Error code
     * @param message   Exception message
     */
    public DCMException(String errorCode, String message) {
        super("Error Message:" + message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor
     *
     * @param errorCode Error code
     */
    public DCMException(String errorCode) {
        this.errorCode = errorCode;
    }

}
