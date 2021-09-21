package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.XmlException;
import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.models.Schema;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.DataRetrieverServiceImpl;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.DomContextServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.shortThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class DataRetrieverServiceTest {

    @Mock
    DataRetrieverService dataRetrieverService;

    @Test
    public void TestRetrieveSchemaBySchemaUrl() throws IOException, XmlconvApiException {
        Schema schema = new Schema();
        schema.setId("8");
        when(dataRetrieverService.retrieveSchemaBySchemaUrl(anyString())).thenReturn(schema);
        Schema result = dataRetrieverService.retrieveSchemaBySchemaUrl("http://waste.eionet.eu.int/schemas/dir200053ec/schema.xsd");
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is("8"));
    }

    @Test
    public void TestGetHostAuthentication() throws IOException, XmlconvApiException {
        when(dataRetrieverService.getHostAuthentication(anyString())).thenReturn("auth");
        String authentication = dataRetrieverService.getHostAuthentication("https://cdrtest.eionet.europa.eu");
        assertThat(authentication, is(notNullValue()));
    }
}
