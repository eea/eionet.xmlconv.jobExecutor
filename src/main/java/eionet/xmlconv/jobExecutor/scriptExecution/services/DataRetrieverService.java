package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.objects.Schema;

import java.util.Hashtable;

public interface DataRetrieverService {

    Schema retrieveSchemaByXmlUrl(String xmlUrl) throws XmlconvApiException;
    Hashtable<String, String> getHostCredentials(String host) throws XmlconvApiException;
}
