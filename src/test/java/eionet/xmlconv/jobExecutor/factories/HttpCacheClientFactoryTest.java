package eionet.xmlconv.jobExecutor.factories;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.SpringApplicationContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class HttpCacheClientFactoryTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void test() {
        CloseableHttpClient client = HttpCacheClientFactory.getInstance((Environment)  SpringApplicationContext.getBean("environment"));
        assertNotNull("Error while requesting client", client);
    }
}
