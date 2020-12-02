package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.objects.Schema;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

@Service
public class DataRetrieverServiceImpl implements DataRetrieverService {

    @Autowired
    public DataRetrieverServiceImpl() {
    }

    @Override
    public Schema retrieveSchemaByXmlUrl(String xmlUrl) throws XmlconvApiException {
        //TODO
        return null;
    }

    @Override
    public Hashtable<String, String> getHostCredentials(String host) throws XmlconvApiException {
        //TODO
        return null;
    }
}
