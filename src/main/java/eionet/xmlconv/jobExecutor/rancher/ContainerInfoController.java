package eionet.xmlconv.jobExecutor.rancher;

import eionet.xmlconv.jobExecutor.rancher.service.ContainerInfoRetriever;
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
        String result = containerInfoRetriever.getContainerName();
        System.out.println(result);
    }
}
