package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.readers;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestConstants;
import eionet.xmlconv.jobExecutor.scriptExecution.services.OpenDocumentSpreadsheetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class OdsReaderServiceImplTest {
    
    @Test
    public void testGetFormulaValueXls2007() throws Exception{
        File inFile = new File(this.getClass().getClassLoader().getResource(TestConstants.SEED_FORMULAS_ODS)
                .getFile());

        OdsReaderServiceImpl odsReader = new OdsReaderServiceImpl();
        odsReader.initReader(inFile);
        OpenDocumentSpreadsheetService spreadsheet = odsReader.getSpreadsheet();
        List<List<String>> tableData = spreadsheet.getTableData(spreadsheet.getTableName(0));

        //test integer formula
        String value = tableData.get(0).get(3);
        assertEquals("2011", value);

        //test string formula
        String value2 = tableData.get(0).get(1);
        assertEquals("EE11", value2);

        //test sum formula
        String value3 = tableData.get(2).get(3);
        assertEquals("4011", value3);

        //test decimal formula
        String value4 = tableData.get(0).get(4);
        assertEquals("2010.123", value4);
    }
}
