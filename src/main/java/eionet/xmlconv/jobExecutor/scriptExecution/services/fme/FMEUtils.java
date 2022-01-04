package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines.FMEQueryEngineServiceImpl;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public final class FMEUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FMEUtils.class);

    public static String createErrorMessage (String fmeJobId, String scriptUrl, String sourceUrl, String exception){
        String resultStringHtml = "<div class=\"feedbacktext\"><span id=\"feedbackStatus\" class=\"BLOCKER\" style=\"display:none\">";
        String resultStringMsg ="The QC Process failed, please allow some time and re-run the process. If the issue persists please contact the dataflow helpdesk. ";
        String resultStringSpecificMsg;
        if (Utils.isNullStr(fmeJobId)){
            resultStringSpecificMsg = "Job submission for script: " + scriptUrl + " and xml url " + sourceUrl + " has failed. ";
        }
        else{
            resultStringSpecificMsg = "The id in the FME server is #" + fmeJobId + ". ";
        }
        String exceptionMsg = "Exception message is: " + exception;
        String fullResultString = resultStringHtml + resultStringMsg + resultStringSpecificMsg + exceptionMsg +  "</span>" ;
        fullResultString += resultStringMsg + resultStringSpecificMsg + exceptionMsg + "</div>";
        return fullResultString;
    }

    public static void handleSynchronousLastRetryExceptionFailure (Integer numberOfRetries, Integer currentRetry, String convertersJobId, String exceptionMsg, String exceptionType, OutputStream result){
        String logMessage = FMEQueryEngineServiceImpl.class.getName() + ": Synchronous job exeuction for job id " + convertersJobId;
        logMessage += " failed with exception "  + exceptionType + " Exception message: "+ exceptionMsg + " for retry "+ currentRetry + " of " + numberOfRetries + " retries";
        LOGGER.error(logMessage);

        // If the last retry fails a BLOCKER predefined error is returned
        if (numberOfRetries == currentRetry){
            try {
                String resultMsg="<div class=\"feedbacktext\"><span id=\"feedbackStatus\" class=\"BLOCKER\" style=\"display:none\">The QC Process failed with " + exceptionType + " for the last retry when trying to contact FME, please allow some time and re-run the process. Please try again. If the issue persists please contact the dataflow helpdesk.</span>"
                        + "The QC Process failed with " + exceptionType + " for the last retry when trying to contact FME, please allow some time and re-run the process. Please try again. If the issue persists please contact the dataflow helpdesk.</div>";
                IOUtils.copy(IOUtils.toInputStream(resultMsg, "UTF-8"), result);
            } catch (IOException ex) {
                LOGGER.error("Could not store result to html file for job id " + convertersJobId);
            }
        }
    }

    public static String constructFMEFolderName(String xmlFileUrl, String randomStr){
        String[] urlSegments = xmlFileUrl.split("/");
        String fileNameWthXml = urlSegments[urlSegments.length-1];
        String[] fileNameSegments = fileNameWthXml.split("\\.");
        String fileName = fileNameSegments[0];
        //if a cdr/bdr folder url was provided, get the folder name
        if(fileName.equals("xml") && (urlSegments.length-2>=0)){
            fileName = urlSegments[urlSegments.length-2];
        }
        String folderName = fileName + "_" +  randomStr;
        return folderName;
    }
}