package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;

import com.ximpleware.*;
import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

@Service
public class VtdHandlerServiceImpl implements XmlHandlerService {

    @Autowired
    public VtdHandlerServiceImpl() {
    }

    @Override
    public boolean parseString(String xml) {
        return false;
    }

    @Override
    public boolean parseString(String xml, OutputStream out) {
        return false;
    }

    @Override
    public void addWarningMessage(String xml, String warningMessage, OutputStream out) throws ScriptExecutionException {
        try {
            VTDGen vg = new VTDGen();
            vg.setDoc(xml.getBytes());
            vg.parse(true);
            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);
            XMLModifier xm = new XMLModifier(vn);
            ap.selectXPath("//div[@class = 'feedbacktext']");
            if (ap.evalXPath() != -1) {
                xm.insertAfterHead("<div class=\"error-msg\">" + warningMessage + "</div>");
            }
            xm.output(out);
            out.flush();
        } catch (UnsupportedEncodingException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (EntityException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (XPathParseException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (ParseException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (NavException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (XPathEvalException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (ModifyException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (TranscodeException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        } catch (IOException e) {
            throw new ScriptExecutionException("Error: " + e.getMessage());
        }
    }
}
