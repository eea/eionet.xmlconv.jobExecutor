package eionet.xmlconv.jobExecutor.datadict;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestConstants;
import eionet.xmlconv.jobExecutor.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class DataDictUtilTest {

    @Test
    public void testNewSchemaDataset() {
        String schemaUrl = "http://dd.eionet.europa.eu/v2/dataset/3381/schema-dst-3381.xsd";
        Map<String, String> map = DataDictUtil.getDatasetReleaseInfoForSchema(schemaUrl);
        assertNotNull(map);
    }

    @Test
    public void testNewSchemaTable() {
        String schemaUrl = "http://dd.eionet.europa.eu/v2/dataset/3381/schema-tbl-11181.xsd";
        Map<String, String> map = DataDictUtil.getDatasetReleaseInfoForSchema(schemaUrl);
        assertNotNull(map);
    }
}
