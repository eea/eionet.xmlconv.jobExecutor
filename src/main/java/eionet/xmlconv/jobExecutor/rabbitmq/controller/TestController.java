package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    Logger logger = LoggerFactory.getLogger(TestController.class);

    @Value("${spring.rabbitmq.host}")
    private String host;

    @GetMapping("/con")
    public void test() {
        logger.info("test connection for host: " + host);
    }
}
