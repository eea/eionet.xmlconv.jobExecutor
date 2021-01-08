package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.models.Schema;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Service
public class DataRetrieverServiceImpl implements DataRetrieverService {

    @Autowired
    public DataRetrieverServiceImpl() {
    }

    @Override
    public Schema retrieveSchemaByXmlUrl(String xmlUrl) throws XmlconvApiException {
        //TODO retrieve Schema from converters by using a query like select * from T_SCHEMA where XML_SCHEMA=xmlUrl
        return null;
    }

    @Override
    public Hashtable<String, String> getHostCredentials(String host) throws XmlconvApiException {
        //TODO returns a hashtable that contains the host's user_name and the pwd
        return null;
    }

    @Override
    public Map getDatasetReleaseInfo(String type, String id) throws Exception {
        String url = Properties.ddURL + Properties.ddReleaseInfoUrl + type + "/" + id;
        HttpGet request = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);
        String jsonStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = new JSONObject(jsonStr);
        Map<String,String> releaseInfo = Utils.convertJsonObjectToHashMap(jsonObject);
        return releaseInfo;
    }


}
