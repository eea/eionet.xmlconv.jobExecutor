package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.QAURIResolverServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import static org.junit.Assert.assertNull;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class QAURIResolverServiceImplTest {

    @Test
    public void testResolve() throws TransformerException {
        URIResolver resolver = new QAURIResolverServiceImpl();
        assertNull(resolver.resolve("http://some.url.ee", ""));
        assertNull(resolver.resolve("script.xquery", ""));
    }
}
