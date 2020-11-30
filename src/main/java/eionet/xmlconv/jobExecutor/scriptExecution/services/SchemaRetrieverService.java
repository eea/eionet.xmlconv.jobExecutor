package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.objects.Schema;

public interface SchemaRetrieverService {

    Schema retrieveSchemaByXmlUrl(String xmlUrl) throws XmlconvApiException;
}
