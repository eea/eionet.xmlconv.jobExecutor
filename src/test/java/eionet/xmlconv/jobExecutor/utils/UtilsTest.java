package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class UtilsTest {
    /**
     * The methods test helper date formatting methods
     *
     * @throws Exception
     */

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

    @Test
    public void testConvertJsonObjectToHashMap() throws Exception {
        JSONObject jsonObject = new JSONObject("{\"a\": 1,\"b\": 2,\"array\": [\"c\",\"d\",\"7\"],\"array of objects\": [{\"e\": 0,\"f\": 5},{\"g\": 1,\"h\": 6}]}");
        Map result = Utils.convertJsonObjectToHashMap(jsonObject);
        assertThat(result.get("a"), is(equalTo(1)));
        assertThat(result.get("b"), is(equalTo(2)));

        ArrayList array = (ArrayList) result.get("array");
        assertThat(array.get(0), is(equalTo("c")));
        assertThat(array.get(1), is(equalTo("d")));
        assertThat(array.get(2), is(equalTo("7")));

        ArrayList arrayOfObjects = (ArrayList) result.get("array of objects");
        assertThat(((HashMap) arrayOfObjects.get(0)).get("e"), is(equalTo(0)));
        assertThat(((HashMap) arrayOfObjects.get(0)).get("f"), is(equalTo(5)));
        assertThat(((HashMap) arrayOfObjects.get(1)).get("g"), is(equalTo(1)));
        assertThat(((HashMap) arrayOfObjects.get(1)).get("h"), is(equalTo(6)));
    }

}
