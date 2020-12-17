package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class HTMLConverterTest {
    @Test
    public void conversionTest() throws Exception {
        InputStream xml = this.getClass().getClassLoader().getResourceAsStream(TestConstants.SEED_DATASET_QA_XML);
        InputStream xsl = this.getClass().getClassLoader().getResourceAsStream("xsl/dummy.xsl");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTMLConverter converter = new HTMLConverter();
        converter.convert(xml, xsl, out, ".html");
        assertEquals("Expected output: ", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy>1</dummy>", new String(out.toByteArray()));
        xml.close();
        xsl.close();
        out.close();
    }
}
