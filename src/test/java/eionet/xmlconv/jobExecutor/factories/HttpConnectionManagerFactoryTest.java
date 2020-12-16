package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Properties;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class HttpConnectionManagerFactoryTest {
    private PoolingHttpClientConnectionManager manager;

    @Test
    public void test() {
        manager = HttpConnectionManagerFactory.getInstance();
        assertEquals("Wrong total connections", Properties.HTTP_MANAGER_TOTAL, manager.getMaxTotal());
        assertEquals("Wrong default route connections", Properties.HTTP_MANAGER_ROUTE, manager.getDefaultMaxPerRoute());
    }
}
