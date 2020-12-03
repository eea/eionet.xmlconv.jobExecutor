package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.exceptions.*;
import eionet.xmlconv.jobExecutor.objects.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request.SubmitJobRequest;

import java.io.OutputStream;

public interface FmeServerCommunicator {


    String submitJob(Script script, SubmitJobRequest request) throws FmeAuthorizationException, FmeCommunicationException;
    FmeJobStatus getJobStatus(String jobId, Script script) throws FmeAuthorizationException, FmeCommunicationException , GenericFMEexception, FMEBadRequestException;
    void getResultFiles(String folderName, OutputStream result) throws FmeAuthorizationException , FMEBadRequestException, GenericFMEexception;
    void deleteFolder(String folderName) throws FmeAuthorizationException , FMEBadRequestException ,GenericFMEexception ;


}