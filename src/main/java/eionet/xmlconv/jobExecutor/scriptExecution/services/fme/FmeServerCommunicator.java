package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SubmitJobRequest;

public interface FmeServerCommunicator {

    String submitJob(Script script, SubmitJobRequest request) throws FmeAuthorizationException, FmeCommunicationException;
    FmeJobStatus getJobStatus(String jobId, Script script) throws FmeAuthorizationException, FmeCommunicationException , GenericFMEexception, FMEBadRequestException;
    void getResultFiles(String jobId, String folderName, String resultFile) throws FmeAuthorizationException , FMEBadRequestException, GenericFMEexception;
    void deleteFolder(String jobId, String folderName) throws FmeAuthorizationException , FMEBadRequestException ,GenericFMEexception ;

}