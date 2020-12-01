package eionet.xmlconv.jobExecutor.rabbitmq.controller;

import eionet.xmlconv.jobExecutor.rabbitmq.service.ContainerInfoRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/container")
public class ContainerInfoController {

    private ContainerInfoRetriever containerInfoRetriever;

    @Autowired
    public ContainerInfoController(ContainerInfoRetriever containerInfoRetriever) {
        this.containerInfoRetriever = containerInfoRetriever;
    }

    @GetMapping("/info")
    public void getInfo() {
        System.out.println("inside method getInfo");
        Object result = containerInfoRetriever.getContainerId();
        System.out.println(result);
    }
}
