package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ConvertersCommunicationException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
public class DataRetrieverServiceImpl implements DataRetrieverService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public DataRetrieverServiceImpl() {
    }

    @Override
    public Schema retrieveSchemaBySchemaUrl(String xmlUrl) throws XmlconvApiException, IOException {
        String url = Properties.convertersUrl + "restapi/" + Properties.convertersSchemaRetrievalUrl;
        HttpGet request = new HttpGet(url);
        request.addHeader("schemaUrl", xmlUrl);
        request.addHeader("Authorization", Properties.convertersEndpointToken);
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
        String url = Properties.convertersUrl + "restapi/" + Properties.convertersHostAuthenticationUrl;
        HttpGet request = new HttpGet(url);
        request.addHeader("hostName", host);
        request.addHeader("Authorization", Properties.convertersEndpointToken);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);
        String authenticatedUser = EntityUtils.toString(response.getEntity());
        return authenticatedUser;
    }

    @Override
    public Map getDatasetReleaseInfo(String type, String id) throws Exception {
        String url = Properties.ddURL + Properties.ddReleaseInfoUrl + type + "/" + id;
        HttpGet request = new HttpGet(url);
        request.addHeader("X-DD-API-KEY", Properties.ddEndpointToken);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);
        String jsonStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = new JSONObject(jsonStr);
        Map<String,String> releaseInfo = Utils.convertJsonObjectToHashMap(jsonObject);
        return releaseInfo;
    }

    @Override
    public Integer getJobStatus(String jobId) throws ConvertersCommunicationException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.add("Authorization", Properties.convertersEndpointToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Integer> result;
        String url = Properties.convertersUrl + "restapi/asynctasks/qajobs/status/" + jobId;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, entity, Integer.class);
        } catch (Exception e) {
            throw new ConvertersCommunicationException("Error retrieving data from converters for job with id " + jobId + ", " + e.getMessage());
        }
        return result.getBody();
    }

}















