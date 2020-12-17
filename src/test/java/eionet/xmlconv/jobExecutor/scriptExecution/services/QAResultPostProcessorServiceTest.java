package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class QAResultPostProcessorServiceTest {

   /* @Autowired
    private DataSource db;*/

    /**
     * Set up test case properties and databaseTester.
     */
  /*  @Before
    public void setUp() throws Exception {
        TestUtils.setUpProperties(this);
        DbHelper.setUpDatabase(db, TestConstants.SEED_DATASET_QA_XML);
    }

    @Test
    public void testExpiredSchemaQAResult() {
        QAResultPostProcessor postProcessor = new QAResultPostProcessor();
        String message = postProcessor.getWarningMessage("http://localhost/not_existing.xsd");

        assertTrue(message.indexOf("expired") > -1);

        String message2 = postProcessor.getWarningMessage("http://localhost/not_existing2.xsd");
        assertNull(message2);
    }

    @Test
    public void testObsoleteDDSchemaQAResult() {
        MockQAResultPostProcessor qaPostProcessor = new MockQAResultPostProcessor();
        Map<String, String> dataset = new HashMap<String, String>();
        dataset.put("id", "1111");
        dataset.put("status", "Released");
        dataset.put("isLatestReleased", "true");
        dataset.put("dateOfLatestReleased", "1257138000000");
        dataset.put("idOfLatestReleased", "2222");
        qaPostProcessor.setDataset(dataset);

        // schema is latest released, no warning message
        String message = qaPostProcessor.getWarningMessage("http://dd.eionet.europa.eu/GetSchema?id=DST1111");
        assertNull(message);

        // schema is not latest released, warning message about obsolete schema
        dataset.put("status", "RELEASED");
        dataset.put("isLatestReleased", "false");
        dataset.put("dateOfLatestReleased", "1257138000000"); // 2 Nov 2009
        dataset.put("idOfLatestReleased", "2222");
        qaPostProcessor.setDataset(dataset);
        String message2 = qaPostProcessor.getWarningMessage("http://dd.eionet.europa.eu/GetSchema?id=DST1111");
        assertTrue(message2.indexOf("obsolete") > -1);

        // schema is not RELEASED, but OK
        dataset.put("status", "Incomplete");
        dataset.put("isLatestReleased", "false");
        qaPostProcessor.setDataset(dataset);
        String message3 = qaPostProcessor.getWarningMessage("http://dd.eionet.europa.eu/GetSchema?id=DST1111");
        assertNull(message3);

    }

    class MockQAResultPostProcessor extends QAResultPostProcessor {

        Map<String, String> datasetResult = null;

        @Override
        protected Map getDataset(String xmlSchema) {
            return datasetResult;
        }

        public void setDataset(Map<String, String> dataset) {
            this.datasetResult = dataset;
        }
    }*/
}
