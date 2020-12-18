package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.DomContextServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class DomContextServiceImplTest {

    @Test
    public void getElementValuesTest() throws XmlException {
        XmlCtxService context = new DomContextServiceImpl();
        context.checkFromString("<xml><test>111</test><test>222</test></xml>");
        XPathQueryService xpath = context.getQueryManager();
        List<String> list = xpath.getElementValues("test");
        assertEquals("Wrong list size", 2, list.size());
    }
}
