package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.HttpFileManagerServiceImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class HttpFileManagerServiceTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private HttpFileManagerService manager;

    @Before
    public void setUp() throws Exception {
        manager = new HttpFileManagerServiceImpl();
    }

    @Test
    public void buildSourceFileUrlWithTicket() throws IOException, URISyntaxException, ScriptExecutionException {
        String url = "http://trustedurl.com";
        String ticket = "ticketValue";

        assertEquals(Properties.convertersUrl + Constants.GETSOURCE_URL + "?ticket=" + ticket + "&source_url=" + url,
                manager.getSourceUrlWithTicket(ticket, url, true));
        assertEquals(url, manager.getSourceUrlWithTicket(null, url, false));
    }

    @Test
    public void testFileProxyUrl() throws IOException, URISyntaxException, ScriptExecutionException {
        exception.expect(URISyntaxException.class);
        String fileUrl = "http://trustedurl.com";
        String url = Properties.convertersUrl + Constants.GETSOURCE_URL + "&source_url=" + fileUrl;
        manager.getFileInputStream(url, null, true);
    }

    @After
    public void tearDown() throws Exception {
        manager.closeQuietly();
    }

    //@Test
    public void testFollowUrlRedirectIfNeededFor301Returned() throws Exception {
        URL toTestUrl = new URL("http://cdrtest.eionet.europa.eu/api/testXMLfile.xml");
        URL spyToTestUrl = Mockito.spy(toTestUrl);
        URL httpsUrl = new URL("https://cdrtest.eionet.europa.eu/api/testXMLfile.xml");
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        HttpFileManagerService httpFileManager = new HttpFileManagerServiceImpl(closeableHttpClient);


        HttpURLConnection mockHttpConnection = mock(HttpURLConnection.class);
        when(spyToTestUrl.openConnection()).thenReturn(mockHttpConnection);
        when(mockHttpConnection.getResponseCode()).thenReturn(301);
        when(mockHttpConnection.getHeaderField(any(String.class))).thenReturn("https://cdrtest.eionet.europa.eu/api/testXMLfile.xml");

        toTestUrl = httpFileManager.followUrlRedirectIfNeeded(toTestUrl);
        assertThat(spyToTestUrl.toString(), equalTo(httpsUrl));
    }
}
