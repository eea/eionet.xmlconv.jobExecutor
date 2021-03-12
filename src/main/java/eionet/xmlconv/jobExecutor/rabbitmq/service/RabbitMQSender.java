package eionet.xmlconv.jobExecutor.rabbitmq.service;

import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobExecutionInfo;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponse;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerStateRabbitMQResponse;

public interface RabbitMQSender {

    /**
     * sends message to rabbitmq containing job results
     * @param response
     */
    void sendMessage(WorkerJobInfoRabbitMQResponse response);

    /**
     * sends message to rabbitmq containing jobExecutor status
     * @param response
     */
    void sendWorkerStatus(WorkerStateRabbitMQResponse response);

    /**
     * sends message to rabbitmq containing information of whether jobExecutor is
     * executing a specific job
     * @param response
     */
    void sendMessageForJobExecution(WorkerJobExecutionInfo response);

}
