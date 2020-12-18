package eionet.xmlconv.jobExecutor.utils;

public class ErrorStorage {
    private String errorMessage = "";
    private String waringMessage = "";
    private String fatalErrorMessage = "";

    /**
     * Default constructor
     */
    public ErrorStorage() {
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets error message
     * @param errorMessage Error message
     */
    public void setErrorMessage(String errorMessage) {
        if (errorMessage.length() > 128)
            errorMessage = errorMessage.substring(0, 128) + "...";
        if (this.errorMessage.length() < 128 && this.errorMessage.indexOf(errorMessage) < 0)
            this.errorMessage += " - " + errorMessage + "  ";
    }

    public String getFatalErrorMessage() {
        return fatalErrorMessage;
    }

    /**
     * Sets fatal error message.
     * @param fatalErrorMessage Fatal error message
     */
    public void setFatalErrorMessage(String fatalErrorMessage) {
        if (fatalErrorMessage.length() > 128)
            fatalErrorMessage = fatalErrorMessage.substring(0, 128) + "...";
        this.fatalErrorMessage += " - " + fatalErrorMessage + "  ";
    }

    public String getWaringMessage() {
        return waringMessage;
    }

    /**
     * Sets warning message
     * @param waringMessage Warning message
     */
    public void setWaringMessage(String waringMessage) {
        if (waringMessage.length() > 128)
            waringMessage = waringMessage.substring(0, 128) + "...";
        this.waringMessage += waringMessage + "  ";
    }

    /**
     * Checks if error message is empty
     * @return True if error message is empty
     */
    public boolean isEmpty() {
        if (errorMessage.equalsIgnoreCase("") && fatalErrorMessage.equalsIgnoreCase("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets errors string
     * @return errors
     */
    public String getErrors() {
        StringBuffer sb = new StringBuffer();
        if (errorMessage != null)
            sb.append(errorMessage);
        if (fatalErrorMessage != null)
            sb.append(fatalErrorMessage);
        return sb.toString();
    }

}
