package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import eionet.xmlconv.jobExecutor.rabbitmq.service.ContainerInfoRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"", "/rancher"})
public class ContainerInfoController {

    private ContainerInfoRetriever containerInfoRetriever;

    @Autowired
    public ContainerInfoController(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @GetMapping("/container/info")
    public String getInfo() {
        System.out.println("inside method test");
        Object result = containerInfoRetriever.getContainerId();
        System.out.println(result);
        return "info retrieved";
    }
}
