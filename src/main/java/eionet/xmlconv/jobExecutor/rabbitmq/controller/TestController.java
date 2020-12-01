package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import eionet.xmlconv.jobExecutor.rabbitmq.service.ContainerInfoRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private ContainerInfoRetriever containerInfoRetriever;

    @Autowired
    public TestController(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @GetMapping("retrieve")
    public void test() {
        Object result = containerInfoRetriever.getContainerId();
        System.out.println(result);
    }
}
