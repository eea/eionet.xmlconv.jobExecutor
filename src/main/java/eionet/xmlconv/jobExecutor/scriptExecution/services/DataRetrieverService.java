package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.models.Schema;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public interface DataRetrieverService {

    Schema retrieveSchemaBySchemaUrl(String xmlUrl) throws XmlconvApiException, IOException;
    Hashtable<String, String> getHostCredentials(String host) throws XmlconvApiException;
    Map getDatasetReleaseInfo(String type, String id) throws Exception;

}
