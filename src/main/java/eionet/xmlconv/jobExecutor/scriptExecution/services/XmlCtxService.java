package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * XML Context Interface.
 */
public interface XmlCtxService {
    /**
     * Sets checking welformedness.
     * @throws XmlException If an error occurs.
     */
    void setWellFormednessChecking() throws XmlException;

    /**
     * Sets validation checking.
     * @throws XmlException If an error occurs.
     */
    void setValidationChecking() throws XmlException;

    /**
     * Validates XML content from input stream.
     * @param inputStream File InputStream
     * @throws XmlException If an error occurs.
     */
    void checkFromInputStream(InputStream inputStream) throws XmlException;

    /**
     * Validates XML content from file.
     * @param fullFileName File name
     * @throws XmlException If an error occurs.
     */
    void checkFromFile(String fullFileName) throws XmlException;

    /**
     * Validates XML content from string.
     * @param xmlString XML String
     * @throws XmlException If an error occurs.
     */
    void checkFromString(String xmlString) throws XmlException;

    /**
     * Creates XML Document
     * @throws XmlException If an error occurs.
     */
    void createXMLDocument() throws XmlException;

    /**
     * Creates XML Document
     * @param docTypeName Doctype name
     * @param systemId System Id
     * @throws XmlException If an error occurs.
     */
    void createXMLDocument(String docTypeName, String systemId) throws XmlException;

    /**
     * Returns XML Manager
     * @return XML Manager
     */
    XmlUpdaterService getManager();

    /**
     * Returns query manager.
     * @return query manager.
     */
    XPathQueryService getQueryManager();

    /**
     * Returns document.
     * @return document
     */
    Document getDocument();

    /**
     * Sets Document
     * @param document Document
     */
    void setDocument(Document document);

}
