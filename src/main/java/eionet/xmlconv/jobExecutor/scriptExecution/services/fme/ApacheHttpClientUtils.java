package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ApacheHttpClientUtils {

    public static JSONObject getJsonFromResponseEntity(HttpEntity entity) throws IOException ,JSONException {
        String jsonStr = EntityUtils.toString(entity);
        JSONObject jsonResponse = new org.json.JSONObject(jsonStr);
        return jsonResponse;
    }

}
