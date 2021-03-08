package eionet.xmlconv.jobExecutor.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobResultInfo {

    @JsonProperty("executionStatus")
    JobExecutionStatus jobExecutionStatus;

    public JobResultInfo() {
    }

    public JobExecutionStatus getJobExecutionStatus() {
        return jobExecutionStatus;
    }

    public JobResultInfo setJobExecutionStatus(JobExecutionStatus jobExecutionStatus) {
        this.jobExecutionStatus = jobExecutionStatus;
        return this;
    }
}
