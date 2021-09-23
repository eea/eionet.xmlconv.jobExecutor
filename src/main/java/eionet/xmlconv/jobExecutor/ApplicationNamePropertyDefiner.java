package eionet.xmlconv.jobExecutor;

import ch.qos.logback.core.PropertyDefinerBase;
import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationNamePropertyDefiner  extends PropertyDefinerBase {


    public static final String GENERIC_JOB_EXECUTOR_NAME="genericJobExecutor";
    public static final String RANCHER_METADATA_URL="http://rancher-metadata/2015-12-19/self/container";


    @Override
    public String getPropertyValue() {
        String containerName = this.getContainerName();
        //Logger not available here
        System.out.println("ApplicationNamePropertyDefiner containerInfo:"+ containerName);
        return String.format("%s", containerName);
    }

    protected String getContainerName() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ContainerInfo> result;
        try {
            result = restTemplate.exchange(RANCHER_METADATA_URL, HttpMethod.GET, entity, ContainerInfo.class);
        } catch (Exception e) {
            //Logger not available here
            System.out.println("Error retrieving rancher metadata: " + e.getMessage());
            return GENERIC_JOB_EXECUTOR_NAME;
        }
        ContainerInfo containerInfo = result.getBody();
        return containerInfo.getName();
    }

}
