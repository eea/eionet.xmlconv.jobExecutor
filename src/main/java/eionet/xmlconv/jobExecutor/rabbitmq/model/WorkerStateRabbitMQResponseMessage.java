package eionet.xmlconv.jobExecutor.rabbitmq.model;

public class WorkerStateRabbitMQResponseMessage extends WorkerMessage {

    private Integer jobExecutorStatus;
    private String healthState;
    private String heartBeatQueue;
    private JobExecutorType jobExecutorType;

    public WorkerStateRabbitMQResponseMessage(WorkerStateRabbitMQResponseBuilder builder) {
        super(builder.jobExecutorName);
        this.jobExecutorStatus = builder.jobExecutorStatus;
        this.healthState = builder.healthState;
        this.heartBeatQueue = builder.heartBeatQueue;
        this.jobExecutorType = builder.jobExecutorType;
    }

    public static class WorkerStateRabbitMQResponseBuilder {
        private String jobExecutorName;
        private Integer jobExecutorStatus;
        private String healthState;
        private String heartBeatQueue;
        private JobExecutorType jobExecutorType;

        public WorkerStateRabbitMQResponseBuilder(String jobExecutorName, Integer jobExecutorStatus) {
            this.jobExecutorName = jobExecutorName;
            this.jobExecutorStatus = jobExecutorStatus;
        }

        public WorkerStateRabbitMQResponseBuilder setJobExecutorName(String jobExecutorName) {
            this.jobExecutorName = jobExecutorName;
            return this;
        }

        public WorkerStateRabbitMQResponseBuilder setJobExecutorStatus(Integer jobExecutorStatus) {
            this.jobExecutorStatus = jobExecutorStatus;
            return this;
        }

        public WorkerStateRabbitMQResponseBuilder setHealthState(String healthState) {
            this.healthState = healthState;
            return this;
        }

        public WorkerStateRabbitMQResponseBuilder setHeartBeatQueue(String heartBeatQueue) {
            this.heartBeatQueue = heartBeatQueue;
            return this;
        }

        public WorkerStateRabbitMQResponseBuilder setJobExecutorType(JobExecutorType jobExecutorType) {
            this.jobExecutorType = jobExecutorType;
            return this;
        }

        public WorkerStateRabbitMQResponseMessage build() {
            return new WorkerStateRabbitMQResponseMessage(this);
        }
    }

    public Integer getJobExecutorStatus() {
        return jobExecutorStatus;
    }

    public String getHealthState() {
        return healthState;
    }

    public String getHeartBeatQueue() {
        return heartBeatQueue;
    }

    public JobExecutorType getJobExecutorType() {
        return jobExecutorType;
    }
}
