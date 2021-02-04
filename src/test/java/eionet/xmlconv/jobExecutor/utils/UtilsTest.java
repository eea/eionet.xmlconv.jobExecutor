package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.models.QAScript;
import eionet.xmlconv.jobExecutor.models.Schema;
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

import static org.hamcrest.CoreMatchers.*;
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

    @Test
    public void testConvertJsonStringToSchema() throws Exception {
        String jsonStr= "{\"id\":\"58\",\"schema\":\"http://localhost/not_existing.xsd\",\"description\":\"Expired dummy\",\"stylesheets\":[],\"isDTD\":false,\"dtdPublicId\":\"\",\"table\":null,\"dataset\":null,\"cdrfiles\":null,\"crfiles\":null,\"datasetReleased\":null,\"doValidation\":false,\"schemaLang\":\"XSD\",\"uplSchemaFileName\":null,\"qascripts\":[{\"query\":\"test_expired-html.xquery\",\"scriptType\":\"xquery 1.0\",\"name\":\"test expired html\",\"description\":\"\",\"id\":\"51\",\"runOnDemandMaxFileSizeMB\":\"10\",\"isActive\":\"1\",\"resultType\":\"HTML\"}],\"uplSchema\":null,\"expireDate\":1289426400000,\"blocker\":false,\"countStylesheets\":0,\"countQaScripts\":0,\"stylesheetSchemaId\":null,\"maxExecutionTime\":null,\"label\":\"http://localhost/not_existing.xsd\",\"ddschema\":false,\"expired\":true}";
        JSONObject jsonObject = new JSONObject(jsonStr);
        Map shemaMap = Utils.convertJsonObjectToHashMap(jsonObject);

        Schema schema = Utils.convertMapToSchema(shemaMap);
        assertThat(schema.getId(), is("58"));
        assertThat(schema.getSchema(), is("http://localhost/not_existing.xsd"));
        assertThat(schema.getDescription(), is("Expired dummy"));
        assertThat(schema.getStylesheets().size(), is(0));
        assertThat(schema.getDtdPublicId(), is(""));
        assertThat(schema.isDoValidation(), is(false));
        assertThat(schema.getSchemaLang(), is("XSD"));
        assertThat(schema.getMaxExecutionTime(), is(nullValue()));

        assertThat(schema.getQascripts().size(), is(1));
        QAScript script = schema.getQascripts().get(0);
        assertThat(script.getScriptId(), is("51"));
        assertThat(script.getDescription(), is(""));
        assertThat(script.getShortName(), is("test expired html"));
        assertThat(script.getFileName(), is("test_expired-html.xquery"));
        assertThat(script.getResultType(), is("HTML"));
        assertThat(script.getScriptType(), is("xquery 1.0"));
        assertThat(script.getUpperLimit(), is("10"));

    }


}
