package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.utils.Utils;

public final class FMEUtils {

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
