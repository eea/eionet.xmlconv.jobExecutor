package eionet.xmlconv.jobExecutor.rancher.service;

import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContainerInfoRetrieverImpl implements ContainerInfoRetriever {

    private RestTemplate restTemplate;
    private static final String RANCHER_METADATA_URL = "http://rancher-metadata/2015-12-19/self/container";

    @Autowired
    public ContainerInfoRetrieverImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getContainerName() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ContainerInfo> result = restTemplate.exchange(RANCHER_METADATA_URL, HttpMethod.GET, entity, ContainerInfo.class);
        ContainerInfo containerInfo = result.getBody();
        return containerInfo.getName();
    }
}

















