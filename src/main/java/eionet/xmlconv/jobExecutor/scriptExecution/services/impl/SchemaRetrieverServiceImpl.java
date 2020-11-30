package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.objects.Schema;
import eionet.xmlconv.jobExecutor.scriptExecution.services.SchemaRetrieverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaRetrieverServiceImpl implements SchemaRetrieverService {

    @Autowired
    public SchemaRetrieverServiceImpl() {
    }

    @Override
    public Schema retrieveSchemaByXmlUrl(String xmlUrl) throws XmlconvApiException {
        //TODO
        return null;
    }
}
