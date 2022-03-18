package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.rabbitmq.config.RabbitMQConfig;
import eionet.xmlconv.jobExecutor.rabbitmq.model.JobExecutorType;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobRabbitMQRequestMessage;

public class GenericHandlerUtils {

    public static WorkerJobRabbitMQRequestMessage createMessageForDeadLetterQueue(WorkerJobRabbitMQRequestMessage request,
                                                                            String errorMessage, Integer errorStatus, String containerName, Integer workerStatus){
        request.setScript(request.getScript());
        request.setErrorMessage(errorMessage);
        request.setErrorStatus(errorStatus);
        request.setJobExecutionRetries(request.getJobExecutionRetries());
        request.setJobExecutorName(containerName);
        request.setJobExecutorStatus(workerStatus);
        request.setHeartBeatQueue(RabbitMQConfig.queue);
        return request;
    }

    public static JobExecutorType getJobExecutorType(Integer rancherJobExecutorType) {
        if (rancherJobExecutorType==null) {
            rancherJobExecutorType = Properties.rancherJobExecutorType;
        }
        JobExecutorType jobExecutorType;
        if (rancherJobExecutorType.equals(JobExecutorType.Light.getId())) {
            jobExecutorType = JobExecutorType.Light;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Heavy.getId())) {
            jobExecutorType = JobExecutorType.Heavy;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Sync_fme.getId())) {
            jobExecutorType = JobExecutorType.Sync_fme;
        } else if (rancherJobExecutorType.equals(JobExecutorType.Async_fme.getId())){
            jobExecutorType = JobExecutorType.Async_fme;
        } else {
            jobExecutorType = JobExecutorType.Unknown;
        }
        return jobExecutorType;
    }
}
