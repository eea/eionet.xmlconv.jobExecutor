package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class SaxonEngineServiceImplTest {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream pr;
    @Before
    public void setUp() {
        pr = new PrintStream(baos);
    }
    @After
    public void tearDown() throws IOException {
        baos.close();
        pr.close();
    }
    @Test
    public void testSimpleQuery() throws Exception {
        Script script = new Script("1 + 3", new String[]{});
        SaxonEngineServiceImpl sax = new SaxonEngineServiceImpl();
        sax.runQuery(script, pr, null);
        Assert.assertEquals("4", baos.toString("UTF-8"));
    }

    @Test(expected = ScriptExecutionException.class)
    public void testException() throws Exception {
        Script xq = new Script("xquery version \"1.0\"; x || y", new String[]{});
        SaxonEngineServiceImpl sax = new SaxonEngineServiceImpl();
        sax.runQuery(xq, pr, null);
    }

}