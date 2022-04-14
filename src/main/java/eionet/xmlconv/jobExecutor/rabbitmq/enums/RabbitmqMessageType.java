package eionet.xmlconv.jobExecutor.rabbitmq.enums;

public enum RabbitmqMessageType {

    JOB_RESPONSE_MESSAGE("job response message"),
    HEART_BEAT_MESSAGE("heart beat message"),
    DEAD_LETTER_MESSAGE("dead letter queue message"),
    WORKER_STATUS_MESSAGE("jobExecutor status message");

    private String value;

    RabbitmqMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
