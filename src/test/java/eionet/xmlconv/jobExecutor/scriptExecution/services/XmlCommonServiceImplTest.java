package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.XmlCommonServiceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class XmlCommonServiceImplTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFromString() throws Exception {
        XmlCommonServiceImpl common = new XmlCommonServiceImpl();
        common.checkFromString("<div>test</div>");
    }

    @Test
    public void checkMalformedXmlFromString() throws Exception {
        exception.expect(XmlException.class);
        XmlCommonServiceImpl common = new XmlCommonServiceImpl();
        common.checkFromString("<div>test<div>");
    }
}
