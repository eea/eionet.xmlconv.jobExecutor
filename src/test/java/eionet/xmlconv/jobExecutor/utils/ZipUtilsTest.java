package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class ZipUtilsTest {
    /**
     * Test ZipUtil unzip method. Exctract the seed...zip file into tmp directory and check that the unzipped file is well-formed
     * XML. The seed zip file should contain 1 well-formed xml file
     *
     * @throws Exception
     */
    @Test
    public void testUnzip() throws Exception {
        String zipSeed = getClass().getClassLoader().getResource(TestConstants.SEED_GENERAL_REPORT_ZIP).getFile();
        String strOutDir = Properties.tmpFolder + File.separator + "unzip";
        ZipUtils.unzip(zipSeed, strOutDir);

        File outDir = new File(strOutDir);
        // test if the directory exists
        assertTrue(outDir.exists());
        assertTrue(outDir.isDirectory());
        // test if the directory has 1 subitem
        assertEquals(1, outDir.list().length);

        File xmlFile = (outDir.listFiles())[0];
        // test if the extracted xml file exists
        assertTrue(xmlFile.exists());
        assertTrue(xmlFile.isFile());
    }
}
