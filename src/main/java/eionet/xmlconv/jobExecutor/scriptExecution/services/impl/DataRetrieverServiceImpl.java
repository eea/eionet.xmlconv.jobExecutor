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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

@Service
public class DataRetrieverServiceImpl implements DataRetrieverService {

    @Autowired
    public DataRetrieverServiceImpl() {
    }

    @Override
    public Schema retrieveSchemaBySchemaUrl(String xmlUrl) throws XmlconvApiException, IOException {
        String url = Properties.convertersUrl + Properties.convertersSchemaRetrievalUrl + xmlUrl;
        HttpGet request = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);
        String jsonStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = new JSONObject(jsonStr);
        Map shemaMap = Utils.convertJsonObjectToHashMap(jsonObject);
        Schema schema = Utils.convertMapToSchema(shemaMap);
        return schema;
    }

    @Override
    public String getHostAuthentication(String host) throws XmlconvApiException, IOException {
        String url = Properties.convertersUrl + Properties.convertersSchemaRetrievalUrl + host;
        HttpGet request = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);
        String authenticatedUser = EntityUtils.toString(response.getEntity());
        return authenticatedUser;
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
