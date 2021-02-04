package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;

import java.io.OutputStream;

public interface ScriptEngineService {

    String DEFAULT_ENCODING = "UTF-8";
    String DEFAULT_OUTPUTTYPE = "html";
    String HTML_CONTENT_TYPE = "html";
    String XML_CONTENT_TYPE = "xml";

    /**
     * processes the XQuery.
     *
     * @param script
     *            the XQscript object with required attributes
     * @return the result of XQuery
     * @throws ScriptExecutionException If an error occurs.
     */
    String getResult(Script script) throws ScriptExecutionException;

    /**
     * Gets result
     * @param script Script
     * @param out OutputStream
     * @throws ScriptExecutionException If an error occurs.
     */
    void getResult(Script script, OutputStream out) throws ScriptExecutionException;

    /**
     * get output type of the XQuery script result. Default is text/html.
     *
     * @return
     */
    String getOutputType();

    /**
     * set output type for XQuery engine. If output type is text/xml, then the XML declaration is omitted to the result.
     *
     * @param outputType Output Type
     */
    void setOutputType(String outputType);

}
