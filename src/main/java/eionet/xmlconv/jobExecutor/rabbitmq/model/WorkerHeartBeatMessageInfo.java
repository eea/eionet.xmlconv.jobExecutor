package eionet.xmlconv.jobExecutor.rabbitmq.model;

import java.sql.Timestamp;

public class WorkerHeartBeatMessageInfo {

    private String jobExecutorName;
    private Integer jobId;
    private Integer jobStatus;
    private Timestamp requestTimestamp;

    public WorkerHeartBeatMessageInfo() {
    }

    public WorkerHeartBeatMessageInfo(String jobExecutorName, Integer jobId) {
        this.jobExecutorName = jobExecutorName;
        this.jobId = jobId;
    }

    public String getJobExecutorName() {
        return jobExecutorName;
    }

    public WorkerHeartBeatMessageInfo setJobExecutorName(String jobExecutorName) {
        this.jobExecutorName = jobExecutorName;
        return this;
    }

    public Integer getJobId() {
        return jobId;
    }

    public WorkerHeartBeatMessageInfo setJobId(Integer jobId) {
        this.jobId = jobId;
        return this;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Timestamp getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Timestamp requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }
}
