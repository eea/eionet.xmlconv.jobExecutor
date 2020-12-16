package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class UrlUtilsTest {

    @Test
    public void getFileNameUnix() {
        String fileName = UrlUtils.getFileName("/home/gso/eea/eionet.xmlconv/target/test-classes/tmp/tmp_1530020988647/seed-general-report.html");
        assertEquals("seed-general-report.html", fileName);
    }

    @Test
    public void getFileNameHttp() {
        String fileName = UrlUtils.getFileName("http://localhost:58081/seed-rivers.xls");
        assertEquals("seed-rivers.xls", fileName);
    }
}
