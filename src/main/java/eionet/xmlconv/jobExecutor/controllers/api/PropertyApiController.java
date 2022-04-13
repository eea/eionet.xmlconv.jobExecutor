package eionet.xmlconv.jobExecutor.controllers.api;

import eionet.xmlconv.jobExecutor.Properties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/properties")
public class PropertyApiController {

    @GetMapping("/get/timeouts")
    public Map<String, String> getTimeoutRelatedProperties() {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("fmeTimeout", String.valueOf(Properties.fmeTimeout));
        hashMap.put("fmeSocketTimeout", String.valueOf(Properties.fmeSocketTimeout));
        hashMap.put("fmeRetryHours", String.valueOf(Properties.fmeRetryHours));
        hashMap.put("httpConnectTimeout", String.valueOf(Properties.HTTP_CONNECT_TIMEOUT));
        hashMap.put("responseTimeoutMs", String.valueOf(Properties.responseTimeoutMs));
        return hashMap;
    }
}
