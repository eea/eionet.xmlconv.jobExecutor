package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;

import java.io.OutputStream;

public interface XmlHandlerService {

    /**
     * Parses XML to find any errors
     * @param xml XML Input
     * @return True if no errors found
     */
    boolean parseString(String xml);

    /**
     * Parses XML to find any errors
     * @param xml XML Input
     * @param out XML Output
     * @return True if no errors found
     */
    boolean parseString(String xml, OutputStream out);

    /**
     * Adds warning message to feedbacktext
     * @param xml Input XML
     * @param warningMessage Warning message
     * @param out Output Stream
     * @throws ScriptExecutionException In case of a parser error
     */
    void addWarningMessage(String xml, String warningMessage, OutputStream out) throws ScriptExecutionException;
}
