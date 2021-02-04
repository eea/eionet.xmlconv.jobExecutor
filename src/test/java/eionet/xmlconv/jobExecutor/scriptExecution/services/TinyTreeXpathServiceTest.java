package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.TinyTreeXpathServiceImpl;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class TinyTreeXpathServiceTest {
    private Processor processor;
    private StreamSource source;

    @Before
    public void setUp() throws Exception {
        processor = new Processor(false);
    }

    @Test
    public void getElementListTest() throws SaxonApiException, XmlException {
        StringReader reader = new StringReader("<root><xml>1</xml><xml>2</xml></root>");
        source = new StreamSource(reader);
        XdmNode root = processor.newDocumentBuilder().build(source);
        XPathQueryService tree = new TinyTreeXpathServiceImpl(processor, root);
        List list = tree.getElementValues("xml");
        assertEquals("Wrong element count", 2, list.size());
    }

    @Test
    public void getElementValuesTest() throws SaxonApiException, XmlException {
        source = new StreamSource(new StringReader("<root><xml name='test1' values='values1'>1</xml><xml name='test2' values='values2'></xml></root>"));
        XdmNode root = processor.newDocumentBuilder().build(source);
        XPathQueryService tree = new TinyTreeXpathServiceImpl(processor, root);
        List<Map<String, String>> list = tree.getElementAttributes("xml");
        assertEquals("Wrong element size", 2, list.size());
        assertEquals("Wrong attribute size", 2, list.get(0).size());
        assertEquals("Wrong attribute value", "values2", list.get(1).get("values"));
    }
}