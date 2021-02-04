package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.datadict.DD_XMLInstance;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DD_XMLInstanceService;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import java.lang.reflect.Method;

@Service
public class DD_XMLInstanceServiceImpl extends DefaultHandler implements DD_XMLInstanceService  {

    private DD_XMLInstance instance = null;

    private static final int root_level = 0;
    private static final int table_level = 1;
    private static final int row_level = 2;
    private static final int element_level = 3;
    private static final String ROW_TAG = "Row";

    private int level = 0;
    private String cur_table = null;

    @Autowired
    public DD_XMLInstanceServiceImpl(){}

    /**
     * Constructor.
     * @param instance XML instance
     */
    public DD_XMLInstanceServiceImpl(DD_XMLInstance instance) {
        this.instance = instance;
    }

    /**
     * Adds namespaces.
     * @param prefix Namespace prefix
     * @param uri Namespace uri
     */
    public void startPrefixMapping(String prefix, String uri) {
        instance.addNamespace(prefix, uri);
    }

    /**
     * Starts element.
     * @param uri Uri
     * @param localName Local name
     * @param name Name
     * @param attributes Attributes
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {

        if (level == root_level) { // root level
            instance.setRootTag(name, localName, attributesToString(attributes));
            level = table_level;
        } else if (level == table_level) { // table_level
            if (localName.equalsIgnoreCase(ROW_TAG)) { // it's table schema an there is only 1 table
                cur_table = instance.getRootTagName();
                instance.setTypeTable();
                instance.addTable(instance.getRootTag());
                instance.addRowAttributes(cur_table, name, attributesToString(attributes));
                level = element_level;
            } else { // it's dataset schema with several tables
                cur_table = name;
                instance.setTypeDataset();
                instance.addTable(name, localName, attributesToString(attributes));
                level = row_level;
            }
        } else if (level == row_level) {
            instance.addRowAttributes(cur_table, name, attributesToString(attributes));
            level = element_level;
        } else if (level == element_level) { // element_level
            instance.addElement(cur_table, name, localName, attributesToString(attributes));
        }
    }

    /**
     * Nothing
     * @param ch ch
     * @param start start
     * @param len len
     */
    public void characters(char[] ch, int start, int len) {
    }

    /**
     * Ends element
     * @param uri Uri
     * @param localName local name
     * @param name name
     */
    @Override
    public void endElement(String uri, String localName, String name) {
        if (level > table_level) {
            if (localName.equalsIgnoreCase(ROW_TAG)) {
                level = table_level;
            }
        }
    }

    /**
     * Converts attributes to String.
     * @param attributes Attributes
     * @return String of attributes
     */
    private String attributesToString(Attributes attributes) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < attributes.getLength(); i++) {
            buf.append(" ");
            buf.append(attributes.getQName(i));
            buf.append("=\"");
            buf.append(attributes.getValue(i));
            buf.append("\"");
        }
        return buf.toString();

    }

    /**
     * Sets document locator
     * @param locator Locator
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        Locator startloc = new LocatorImpl(locator);
        String encoding = getEncoding(startloc);
        if (!Utils.isNullStr(encoding))
            instance.setEncoding(encoding);
    }

    /**
     * Gets Locator encoding.
     * @param locator Locator
     * @return Locator encoding
     */
    private String getEncoding(Locator locator) {
        String encoding = null;
        Method getEncoding = null;
        try {
            getEncoding = locator.getClass().getMethod("getEncoding", new Class[] {});
            if (getEncoding != null) {
                encoding = (String) getEncoding.invoke(locator);
            }
        } catch (Exception e) {
            // either this locator object doesn't have this
            // method, or we're on an old JDK
        }
        return encoding;
    }
}
