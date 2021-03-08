package eionet.xmlconv.jobExecutor.models;

public class JobExecutionStatus {

    Integer statusId;
    String statusName;

    public JobExecutionStatus() {
    }

    public JobExecutionStatus(Integer statusId, String statusName) {
        this.statusId = statusId;
        this.statusName = statusName;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public JobExecutionStatus setStatusId(Integer statusId) {
        this.statusId = statusId;
        return this;
    }

    public String getStatusName() {
        return statusName;
    }

    public JobExecutionStatus setStatusName(String statusName) {
        this.statusName = statusName;
        return this;
    }
}
