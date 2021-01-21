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
    public void testMultivalueElementsDefs() throws Exception {
        String schemaUrl = TestUtils.getSeedURL(TestConstants.SEED_GW_SCHEMA, this);

        Map<String, DDElement> elemDefs = DataDictUtil.importDDTableSchemaElemDefs(schemaUrl);

        DDElement stratElement = elemDefs.get("Stratigraphy");
        assertTrue(stratElement.isHasMultipleValues());
        assertEquals(";", stratElement.getDelimiter());

    }

    @Test
    public void testNewSchemaDataset() {
        String schemaUrl = "https://dd.ewxdevel1dub.eionet.europa.eu/v2/dataset/3381/schema-dst-3381.xsd";
        Map<String, String> map = DataDictUtil.getDatasetReleaseInfoForSchema(schemaUrl);
        assertNotNull(map);
    }

    @Test
    public void testNewSchemaTable() {
        String schemaUrl = "https://dd.ewxdevel1dub.eionet.europa.eu/v2/dataset/3381/schema-tbl-11181.xsd";
        Map<String, String> map = DataDictUtil.getDatasetReleaseInfoForSchema(schemaUrl);
        assertNotNull(map);
    }


    @Test
    public void testGetElementsDefs() throws Exception {
        String schemaUrl = TestUtils.getSeedURL(TestConstants.SEED_GW_CONTAINER_SCHEMA, this);
        Map<String, DDElement> elemDefs = DataDictUtil.importDDElementSchemaDefs(null, schemaUrl);
        assertEquals(43, elemDefs.size());

        String type = elemDefs.get("GWEWN-Code").getSchemaDataType();
        assertEquals("xs:string", type);

        String type2 = elemDefs.get("GWArea").getSchemaDataType();
        assertEquals("xs:decimal", type2);
    }
}
