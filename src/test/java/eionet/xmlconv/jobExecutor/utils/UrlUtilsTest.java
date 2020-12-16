package eionet.xmlconv.jobExecutor.utils;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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
