package eionet.xmlconv.jobExecutor.rancher.service;

import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContainerInfoRetrieverImpl implements ContainerInfoRetriever {

    @Value("${rancher.metadata.container.url}")
    private String rancherMetadataUrl;

    private RestTemplate restTemplate;

    @Autowired
    public ContainerInfoRetrieverImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getContainerName() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ContainerInfo> result = restTemplate.exchange(rancherMetadataUrl, HttpMethod.GET, entity, ContainerInfo.class);
        ContainerInfo containerInfo = result.getBody();
        return containerInfo.getName();
    }
}

















