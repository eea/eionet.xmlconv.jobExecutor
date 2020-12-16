package eionet.xmlconv.jobExecutor.converters;

import eionet.xmlconv.jobExecutor.TestConstants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
public class ExcelConverterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void convertExcel() throws Exception {
        InputStream xml = this.getClass().getClassLoader().getResourceAsStream(TestConstants.SEED_DATASET_QA_XML);
        InputStream xsl = this.getClass().getClassLoader().getResourceAsStream("xsl/dummy.xsl");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelConverter converter = new ExcelConverter();
        converter.convert(xml, xsl, out, ".xls");
        assertEquals("Expected size: ", 3584, out.size());
        xml.close();
        xsl.close();
        out.close();
    }
}
