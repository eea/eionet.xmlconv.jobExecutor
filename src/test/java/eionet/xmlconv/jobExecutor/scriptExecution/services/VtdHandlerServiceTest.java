package eionet.xmlconv.jobExecutor.scriptExecution.services;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.VtdHandlerServiceImpl;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class VtdHandlerServiceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void VtdModifierTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String xml = "<html><div class=\"feedbacktext\">test</div></html>";
        VTDGen vg = new VTDGen();
        vg.setDoc(xml.getBytes());
        vg.parse(true);
        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        XMLModifier xm = new XMLModifier(vn);
        ap.selectXPath("//div[@class = 'feedbacktext']");
        if (ap.evalXPath() != -1) {
            xm.insertAfterHead("<div class=\"error-msg\">Test</div>");
        }
        xm.output(out);
        out.flush();
        assertEquals("Wrong result, XML parser error: ", "<html><div class=\"feedbacktext\"><div class=\"error-msg\">Test</div>test</div></html>", new String(out.toByteArray()));
    }

    @Test
    public void addWarningMessageTest() throws ScriptExecutionException, IOException {
        XmlHandlerService handler = new VtdHandlerServiceImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String xml = "<html><div class=\"feedbacktext\">test</div></html>";
        String desiredOutput = "<html><div class=\"feedbacktext\"><div class=\"error-msg\">test warning message</div>test</div></html>";
        handler.addWarningMessage(xml, "test warning message", out);
        String output = new String(out.toByteArray());
        assertEquals("Expecting output result to include warning message: ", output, desiredOutput);
        out.close();
    }

    @Test
    public void addWarningWrongInput() throws ScriptExecutionException, IOException {
        XmlHandlerService handler = new VtdHandlerServiceImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String xml = "<html><div class=\"feedback\">test</div></html>";
        handler.addWarningMessage(xml, "test warning message", out);
        String output = new String(out.toByteArray());
        assertEquals("Expecting output to be equal with input: ", output, xml);
        out.close();
    }

    @Test
    public void addWarningNoXML() throws ScriptExecutionException, IOException {
        exception.expect(ScriptExecutionException.class);
        XmlHandlerService handler = new VtdHandlerServiceImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String xml = "test";
        handler.addWarningMessage(xml, "test warning message", out);
        out.close();
    }

    @Test
    public void dummyParseString() throws IOException {
        XmlHandlerService handler = new VtdHandlerServiceImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertFalse(handler.parseString("Test"));
        assertFalse(handler.parseString("Test", out));
        out.close();
    }
}

