package eionet.xmlconv.jobExecutor.scriptExecution.services;


import eionet.xmlconv.jobExecutor.exceptions.XmlException;

/**
 * XUpdate interface.
 */
public interface XmlUpdaterService {
    /**
     * Inserts element
     * @param parentElementName Parent element name
     * @param elementName element name
     * @throws XmlException If an error occurs.
     */
    void insertElement(String parentElementName, String elementName) throws XmlException;

    /**
     * Inserts Attribute
     * @param parentElementName Parent element name
     * @param attributeName Attribute name
     * @param attributeValue Attribute value
     * @throws XmlException If an error occurs.
     */
    void insertAttribute(String parentElementName, String attributeName, String attributeValue) throws XmlException;

    /**
     * Update text value of the existing XML element.
     * @param parentId Parent Id
     * @param name Name
     * @param newValue New Value
     * @throws XmlException If an error occurs.
     */
    void updateElement(String parentId, String name, String newValue) throws XmlException;

    /**
     * Deletes element
     * @param parentId Parent Id
     * @param name Name
     * @throws XmlException If an error occurs.
     */
    void deleteElement(String parentId, String name) throws XmlException;

}
