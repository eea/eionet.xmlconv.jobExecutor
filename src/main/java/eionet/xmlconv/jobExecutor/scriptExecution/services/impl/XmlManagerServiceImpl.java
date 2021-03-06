package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlCtxService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlUpdaterService;
import org.apache.xpath.XPathAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.transform.TransformerException;

@Service
public class XmlManagerServiceImpl implements XmlUpdaterService {

    protected XmlCtxService ctx = null;

    /**
     * Default constructor.
     */
    @Autowired
    public XmlManagerServiceImpl() {
    }

    /**
     * Constructor
     * @param ctx Context
     */
    public XmlManagerServiceImpl(XmlCtxService ctx) {
        this.ctx = ctx;
    }

    /**
     * Update elements
     * @param parentId Parent Id
     * @param name Name
     * @param newValue New Value
     * @throws XmlException If an error occurs.
     */
    @Override
    public void updateElement(String parentId, String name, String newValue) throws XmlException {
        String xpath = "//*[@id='" + parentId + "']/" + name + "/text()";
        Node textNode = null;
        try {
            textNode = XPathAPI.selectSingleNode(ctx.getDocument(), xpath);
            if (textNode == null) {
                textNode = XPathAPI.selectSingleNode(ctx.getDocument(), "//" + name);
                if (textNode == null)
                    throw new XmlException("Node " + name + " can not be found");
                else {
                    Text newElement = ctx.getDocument().createTextNode(newValue);
                    textNode.appendChild(newElement);
                }
            } else {
                Node parent = textNode.getParentNode();
                Text newElement = ctx.getDocument().createTextNode(newValue);
                parent.replaceChild(newElement, textNode);
            }
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new XmlException("Error while setting value to element " + name + ": " + e.getMessage());
        }
    }

    /**
     * Deletes element
     * @param parentId Parent Id
     * @param name Name
     * @throws XmlException If an error occurs.
     */
    @Override
    public void deleteElement(String parentId, String name) throws XmlException {
        try {
            String xpath = "//*[@id='" + parentId + "']/" + name;
            Node node = XPathAPI.selectSingleNode(ctx.getDocument(), xpath);
            if (node == null)
                return;
            Node parent = node.getParentNode();
            ((Element) parent).removeChild(node);
        } catch (Exception e) {
            throw new XmlException("Error while removing XML node " + name + ": " + e.getMessage());
        }
    }

    /**
     * Insert element
     * @param parentElementName Parent element name
     * @param elementName element name
     * @throws XmlException If an error occurs.
     */
    @Override
    public void insertElement(String parentElementName, String elementName) throws XmlException {
    }

    /**
     * Inserts attribute
     * @param parentElementName Parent element name
     * @param attributeName Attribute name
     * @param attributeValue Attribute value
     * @throws XmlException If an error occurs.
     */
    @Override
    public void insertAttribute(String parentElementName, String attributeName, String attributeValue) throws XmlException {
    }

}
