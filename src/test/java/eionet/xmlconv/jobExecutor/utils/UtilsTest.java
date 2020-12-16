package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
public class UtilsTest {
    /**
     * The methods test helper date formatting methods
     *
     * @throws Exception
     */

    @Before
    public void setUp() {
        TestUtils.setUpProperties(this);
    }

    @Test
    public void testDateTime() throws Exception {

        String strDate = "06.02.2008";
        String pattern = "dd.MM.yyyy";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
        Date date = dateFormatter.parse(strDate);

        assertEquals(date, Utils.parseDate(strDate, pattern));
        assertEquals(strDate, Utils.getFormat(date, pattern));
        assertEquals(strDate, Utils.getFormat(Utils.parseDate(strDate, pattern), pattern));
        // use default date and time formats from gdem.properties files
        assertEquals(Utils.getFormat(date, Properties.dateFormatPattern), Utils.getDate(date));
        assertEquals(Utils.getFormat(date, Properties.timeFormatPattern), Utils.getDateTime(date));

    }

    @Test
    public void testEscapeXml() {
        assertEquals("&amp;ok", Utils.escapeXML("&ok"));
        assertEquals("&amp;ok", Utils.escapeXML("&amp;ok"));
        assertEquals("?", Utils.escapeXML("\u001A"));
        assertEquals("&#57344;", Utils.escapeXML("\uE000"));
        assertEquals("\u00F6", Utils.escapeXML("\u00F6"));
    }

    @Test
    public void testGetTmpUniqueFileName() {
        assertTrue(Utils.getUniqueTmpFileName(null).endsWith(".tmp"));
        assertTrue(Utils.getUniqueTmpFileName("filename.xml").endsWith("filename.xml"));
        assertTrue(Utils.getUniqueTmpFileName(null).startsWith(Properties.tmpFolder + File.separator + Constants.TMP_FILE_PREFIX));
    }
}
