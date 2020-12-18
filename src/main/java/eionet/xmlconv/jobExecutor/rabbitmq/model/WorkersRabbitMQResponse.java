package eionet.xmlconv.jobExecutor.rabbitmq.model;

import eionet.xmlconv.jobExecutor.models.Script;

public class WorkersRabbitMQResponse {

    private Script xqScript;

    private int jobStatus;

    private boolean errorExists;

    private String errorMessage;

    private String containerName;

    private String executionTime;

    public Script getXqScript() {
        return xqScript;
    }

    public WorkersRabbitMQResponse setXqScript(Script xqScript) {
        this.xqScript = xqScript;
        return this;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public WorkersRabbitMQResponse setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public boolean isErrorExists() {
        return errorExists;
    }

    public WorkersRabbitMQResponse setErrorExists(boolean errorExists) {
        this.errorExists = errorExists;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WorkersRabbitMQResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getContainerName() {
        return containerName;
    }

    public WorkersRabbitMQResponse setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public WorkersRabbitMQResponse setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
        return this;
    }
}