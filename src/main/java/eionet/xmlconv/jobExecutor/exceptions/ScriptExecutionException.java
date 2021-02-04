package eionet.xmlconv.jobExecutor.exceptions;

public class ScriptExecutionException extends Exception {
    /**
     * Constructs a new exception with null as its detail message.
     */
    public ScriptExecutionException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param s Exception message
     */
    public ScriptExecutionException(String s) {
        super(s);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param s Exception message
     * @param cause Exception cause
     */
    public ScriptExecutionException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of
     * (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of cause).
     * @param cause Exception cause
     */
    public ScriptExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     * @param s Exception message
     * @param cause Exception cause
     * @param b supression enabled
     * @param b1 writable stack trace enabled
     */
    protected ScriptExecutionException(String s, Throwable cause, boolean b, boolean b1) {
        super(s, cause, b, b1);
    }
}
