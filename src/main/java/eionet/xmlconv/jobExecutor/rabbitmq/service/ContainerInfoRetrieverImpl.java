package eionet.xmlconv.jobExecutor.rabbitmq.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContainerInfoRetrieverImpl implements ContainerInfoRetriever {

    private RestTemplate restTemplate;

    @Autowired
    public ContainerInfoRetrieverImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object getContainerId() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Object result = restTemplate.exchange("http://rancher-metadata/2015-12-19/self/container", HttpMethod.GET, entity, Object.class);
        System.out.println(result);
        return result;
    }
}
