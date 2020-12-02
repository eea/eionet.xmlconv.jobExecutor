package eionet.xmlconv.jobExecutor.scriptExecution.services;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public interface DD_XMLInstanceService {
    void startElement(String uri, String localName, String name, Attributes attributes);
    void endElement(String uri, String localName, String name);
    void setDocumentLocator(Locator locator);


}
