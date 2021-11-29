package eionet.xmlconv.jobExecutor.rancher.service;

import eionet.xmlconv.jobExecutor.rancher.entity.ContainerInfo;

public interface ContainerInfoRetriever {

    /**
     * retrieves container name
     * @return
     */
    String getContainerName();

    /**
     * retrieves information about the container
     * @return
     */
    ContainerInfo getContainerInfo();
}
