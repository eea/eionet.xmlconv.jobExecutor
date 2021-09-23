package eionet.xmlconv.jobExecutor.rabbitmq.model;

public class WorkerStateRabbitMQResponse {

    private String jobExecutorName;
    private Integer jobExecutorStatus;
    private String healthState;
    private String heartBeatQueue;
    private JobExecutorType jobExecutorType;

    public WorkerStateRabbitMQResponse(WorkerStateRabbitMQResponseBuilder builder) {
        this.jobExecutorName = builder.jobExecutorName;
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

        public WorkerStateRabbitMQResponse build() {
            return new WorkerStateRabbitMQResponse(this);
        }
    }

    public String getJobExecutorName() {
        return jobExecutorName;
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
