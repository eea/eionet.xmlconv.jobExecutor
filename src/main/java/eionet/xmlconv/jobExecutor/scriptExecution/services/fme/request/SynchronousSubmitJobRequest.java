package eionet.xmlconv.jobExecutor.scriptExecution.services.fme.request;


import eionet.xmlconv.jobExecutor.Properties;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class SynchronousSubmitJobRequest extends SubmitJobRequest {

    private String xmlSourceFile;
    private String folderName;

    ArrayList<NameValuePair> postParameters;

    private String fmeResultFolderProperty = Properties.fmeResultFolder;

    public SynchronousSubmitJobRequest(String xmlSourceFile, String folderName) {
        super(xmlSourceFile);
        this.xmlSourceFile = xmlSourceFile;
        this.folderName = folderName;
    }

    @Override
    public String buildBody() {
        JSONObject folderObj=new JSONObject();
        folderObj.put(NAME_KEY, FOLDER_VALUE);
        folderObj.put(VALUE_KEY, this.fmeResultFolderProperty + "/" +folderName);

        JSONObject envelObj=new JSONObject();
        envelObj.put(NAME_KEY, ENVELOPE_VALUE_PARAM);
        envelObj.put(VALUE_KEY, xmlSourceFile);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(folderObj);
        jsonArray.put(envelObj);

        JSONObject result = new JSONObject();
        result.put("publishedParameters",jsonArray);
        return  result.toString();
    }


}
