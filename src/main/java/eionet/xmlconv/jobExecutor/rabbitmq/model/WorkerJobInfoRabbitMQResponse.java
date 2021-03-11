package eionet.xmlconv.jobExecutor.rabbitmq.model;

import eionet.xmlconv.jobExecutor.models.Script;

public class WorkerJobInfoRabbitMQResponse {

    private Script script;

    private boolean errorExists;

    private String errorMessage;

    private String jobExecutorName;

    private String executionTime;

    private Integer jobExecutorStatus;

    public Script getScript() {
        return script;
    }

    public WorkerJobInfoRabbitMQResponse setScript(Script script) {
        this.script = script;
        return this;
    }

    public boolean isErrorExists() {
        return errorExists;
    }

    public WorkerJobInfoRabbitMQResponse setErrorExists(boolean errorExists) {
        this.errorExists = errorExists;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WorkerJobInfoRabbitMQResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getJobExecutorName() {
        return jobExecutorName;
    }

    public WorkerJobInfoRabbitMQResponse setJobExecutorName(String jobExecutorName) {
        this.jobExecutorName = jobExecutorName;
        return this;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public WorkerJobInfoRabbitMQResponse setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    public Integer getJobExecutorStatus() {
        return jobExecutorStatus;
    }

    public WorkerJobInfoRabbitMQResponse setJobExecutorStatus(Integer jobExecutorStatus) {
        this.jobExecutorStatus = jobExecutorStatus;
        return this;
    }

}
