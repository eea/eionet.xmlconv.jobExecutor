package eionet.xmlconv.jobExecutor.rabbitmq.model;

import eionet.xmlconv.jobExecutor.models.Script;

public class WorkerJobInfoRabbitMQResponseMessage extends WorkerMessage {

    private Script script;

    private boolean errorExists;

    private String errorMessage;

    private String executionTime;

    private Integer jobExecutorStatus;

    private String heartBeatQueue;

    private Integer errorStatus;

    private Integer jobExecutionRetries;

    private JobExecutorType jobExecutorType;


    public Script getScript() {
        return script;
    }

    public WorkerJobInfoRabbitMQResponseMessage setScript(Script script) {
        this.script = script;
        return this;
    }

    public boolean isErrorExists() {
        return errorExists;
    }

    public WorkerJobInfoRabbitMQResponseMessage setErrorExists(boolean errorExists) {
        this.errorExists = errorExists;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WorkerJobInfoRabbitMQResponseMessage setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public WorkerJobInfoRabbitMQResponseMessage setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    public Integer getJobExecutorStatus() {
        return jobExecutorStatus;
    }

    public WorkerJobInfoRabbitMQResponseMessage setJobExecutorStatus(Integer jobExecutorStatus) {
        this.jobExecutorStatus = jobExecutorStatus;
        return this;
    }

    public String getHeartBeatQueue() {
        return heartBeatQueue;
    }

    public WorkerJobInfoRabbitMQResponseMessage setHeartBeatQueue(String heartBeatQueue) {
        this.heartBeatQueue = heartBeatQueue;
        return this;
    }

    public Integer getErrorStatus() {
        return errorStatus;
    }

    public WorkerJobInfoRabbitMQResponseMessage setErrorStatus(Integer errorStatus) {
        this.errorStatus = errorStatus;
        return this;
    }

    public Integer getJobExecutionRetries() {
        return jobExecutionRetries;
    }

    public WorkerJobInfoRabbitMQResponseMessage setJobExecutionRetries(Integer jobExecutionRetries) {
        this.jobExecutionRetries = jobExecutionRetries;
        return this;
    }

    public JobExecutorType getJobExecutorType() {
        return jobExecutorType;
    }

    public WorkerJobInfoRabbitMQResponseMessage setJobExecutorType(JobExecutorType jobExecutorType) {
        this.jobExecutorType = jobExecutorType;
        return this;
    }
}
