package eionet.xmlconv.jobExecutor.rancher.service;

import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class ContainerInfoRetrieverImpl implements ContainerInfoRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerInfoRetrieverImpl.class);

    @Value("${rancher.metadata.container.url}")
    private String rancherMetadataUrl;

    private RestTemplate restTemplate;
    private Environment env;

    private static final String DEV_PROFILE = "dev";
    private static final String DEMO_CONTAINER_NAME = "demoJobExecutor";
    private static final String DEMO_SERVICE_NAME = "demoService";

    @Autowired
    public ContainerInfoRetrieverImpl(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;
    }

    @Override
    public String getContainerName() {
        boolean devProfile = Arrays.asList(env.getActiveProfiles()).stream().allMatch(p -> p.equals(DEV_PROFILE));
        if (devProfile) {
            return DEMO_CONTAINER_NAME;
        }
        ContainerInfo containerInfo = getContainerInfo();
        return containerInfo.getName();
    }

    @Override
    public ContainerInfo getContainerInfo() {
        boolean devProfile = Arrays.asList(env.getActiveProfiles()).stream().allMatch(p -> p.equals(DEV_PROFILE));
        if (devProfile) {
            ContainerInfo devContainer = new ContainerInfo();
            devContainer.setName(DEMO_CONTAINER_NAME);
            devContainer.setService_name(DEMO_SERVICE_NAME);
            return devContainer;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ContainerInfo> result;
        try {
            result = restTemplate.exchange(rancherMetadataUrl, HttpMethod.GET, entity, ContainerInfo.class);
        } catch (Exception e) {
            LOGGER.info("Error retrieving rancher metadata: " + e.getMessage());
            return new ContainerInfo();
        }
        return result.getBody();
    }
}

















