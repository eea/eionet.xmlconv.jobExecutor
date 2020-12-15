package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service("xgawkEngineService")
public class XGawkQueryEngineServiceImpl extends ExternalQueryEngineServiceImpl{

    @Autowired
    public XGawkQueryEngineServiceImpl() {
    }

    @Override
    protected String getShellCommand(String dataFile, String scriptFile, Map<String, String> params) {
        return Properties.xgawkCommand + getVariables(params) + " -f " + scriptFile + " " + dataFile;
    }

    /**
     * Gets variables
     * @param params Parameters
     * @return Variables
     */
    protected String getVariables(Map<String, String> params) {

        String ret = "";
        if (!Utils.isNullHashMap(params)) {
            StringBuffer buf = new StringBuffer();
            Iterator<String> it = params.keySet().iterator();

            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                buf.append(" -v ");
                buf.append(key);
                buf.append("=\"");
                buf.append(value);
                buf.append("\"");
            }
            ret = buf.toString();
        }

        return ret;
    }
}
