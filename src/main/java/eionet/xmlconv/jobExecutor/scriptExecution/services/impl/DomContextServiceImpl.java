package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlCtxService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlUpdaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class DomContextServiceImpl extends XmlCommonServiceImpl implements XmlCtxService {
    /**
     * Default constructor
     */
    @Autowired
    public DomContextServiceImpl() {
    }

    public XmlUpdaterService getManager() {
        return new XmlManagerServiceImpl(this);
    }

    public DomXpathServiceImpl getQueryManager() {
        return new DomXpathServiceImpl(this);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
