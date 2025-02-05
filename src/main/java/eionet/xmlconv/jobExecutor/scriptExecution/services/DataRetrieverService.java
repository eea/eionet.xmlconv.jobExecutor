package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.models.Schema;

import java.io.IOException;
import java.util.Map;

public interface DataRetrieverService {

    Schema retrieveSchemaBySchemaUrl(String xmlUrl) throws XmlconvApiException, IOException;
    String getHostAuthentication(String host) throws XmlconvApiException, IOException;
    Map getDatasetReleaseInfo(String type, String id) throws Exception;
    Integer getJobStatus(String jobId) throws ConvertersCommunicationException;
}
