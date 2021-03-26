package eionet.xmlconv.jobExecutor.rabbitmq.model;

import eionet.xmlconv.jobExecutor.models.Script;

public class WorkerJobRabbitMQRequest {

    private Script script;
    private String jobExecutorName;
    private Integer jobExecutionRetries;

    public Script getScript() {
        return script;
    }

    public WorkerJobRabbitMQRequest setScript(Script script) {
        this.script = script;
        return this;
    }

    public String getJobExecutorName() {
        return jobExecutorName;
    }

    public WorkerJobRabbitMQRequest setJobExecutorName(String jobExecutorName) {
        this.jobExecutorName = jobExecutorName;
        return this;
    }

    public Integer getJobExecutionRetries() {
        return jobExecutionRetries;
    }

    public void setJobExecutionRetries(Integer jobExecutionRetries) {
        this.jobExecutionRetries = jobExecutionRetries;
    }
}
