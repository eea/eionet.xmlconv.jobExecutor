package eionet.xmlconv.jobExecutor.converters;

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
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class OdsConverterTest {

    @Test
    public void conversionTest() throws Exception {
        Properties.odsFolder = "src/test/resources/ods";
        OdsConverter converter = new OdsConverter();
        InputStream xml = this.getClass().getClassLoader().getResourceAsStream(TestConstants.SEED_DATASET_QA_XML);
        InputStream xsl = this.getClass().getClassLoader().getResourceAsStream("xsl/dummy.xsl");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        converter.convert(xml, xsl, out, ".ods");
        assertEquals("Expected size: ", 4886, out.size());
        xml.close();
        xsl.close();
        out.close();
    }
}
