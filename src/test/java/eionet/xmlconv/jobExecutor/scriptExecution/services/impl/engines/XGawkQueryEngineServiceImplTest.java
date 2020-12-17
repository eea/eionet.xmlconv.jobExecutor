package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class XGawkQueryEngineServiceImplTest {
    @Test
    public void testGetShellCommand() {
        String dataFile = "data.xml";
        String scriptFile = "script.xml";

        XGawkQueryEngineServiceImpl engine = new XGawkQueryEngineServiceImpl();
        String command = engine.getShellCommand(dataFile, scriptFile, null);

        assertEquals(Properties.xgawkCommand + " -f script.xml data.xml", command);
    }

    @Test
    public void testGetShellCommandWithParams() throws Exception {
        String dataFile = "data.xml";
        String scriptFile = "script.xml";
        Map params = new TreeMap<String, String>();
        params.put("param2", "param2value");
        params.put("source_url", "http://localhost/dummy.xml");

        XGawkQueryEngineServiceImpl engine = new XGawkQueryEngineServiceImpl();
        String command = engine.getShellCommand(dataFile, scriptFile, params);

        assertEquals(Properties.xgawkCommand + " -v param2=\"param2value\" -v source_url=\"http://localhost/dummy.xml\" "
                + "-f script.xml data.xml", command);

    }
}
