package eionet.xmlconv.jobExecutor.rabbitmq.model;

import eionet.xmlconv.jobExecutor.models.Script;

public class WorkersRabbitMQResponse {

    private Script script;

    private boolean errorExists;

    private String errorMessage;

    private String containerName;

    private String executionTime;

    private Integer jobExecutorStatus;

    public Script getScript() {
        return script;
    }

    public WorkersRabbitMQResponse setScript(Script script) {
        this.script = script;
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

    public Integer getJobExecutorStatus() {
        return jobExecutorStatus;
    }

    public WorkersRabbitMQResponse setJobExecutorStatus(Integer jobExecutorStatus) {
        this.jobExecutorStatus = jobExecutorStatus;
        return this;
    }
}
