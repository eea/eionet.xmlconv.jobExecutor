package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.scriptExecution.processors.SaxonProcessor;
import eionet.xmlconv.jobExecutor.utils.Utils;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;

@Service("saxonEngineService")
public class SaxonEngineServiceImpl extends ScriptEngineServiceImpl {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SaxonEngineServiceImpl.class);

    /**
     * Default Constructor
     * @throws ScriptExecutionException If an error occurs.
     */
    @Autowired
    public SaxonEngineServiceImpl() throws ScriptExecutionException {
    }

    @Override
    protected void runQuery(Script script, OutputStream result, WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException {

        Processor proc = SaxonProcessor.getProcessor();
        XQueryCompiler comp = proc.newXQueryCompiler();

        String queriesPathURI = Utils.getURIfromPath(Properties.queriesFolder, true);
        comp.setBaseURI(URI.create(queriesPathURI));

        Reader queryReader = null;
        try {
            Serializer out = proc.newSerializer(result);
            out.setOutputProperty(Serializer.Property.INDENT, "no");
            out.setOutputProperty(Serializer.Property.ENCODING, DEFAULT_ENCODING);
            // if the output is html, then use method="xml" in output, otherwise, it's not valid xml
            if (getOutputType().equals(HTML_CONTENT_TYPE)) {
                out.setOutputProperty(Serializer.Property.METHOD, XML_CONTENT_TYPE);
            } else {
                out.setOutputProperty(Serializer.Property.METHOD, getOutputType());
            }
            // add xml declaration only, if the output should be XML
            if (getOutputType().equals(XML_CONTENT_TYPE)) {
                out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "no");
            } else {
                out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            }
            if (!Utils.isNullStr(script.getScriptSource())) {
                queryReader = new StringReader(script.getScriptSource());
            } else if (!Utils.isNullStr(script.getScriptFileName())) {
                queryReader = new FileReader(script.getScriptFileName());
            } else {
                throw new ScriptExecutionException("XQuery engine could not find script source or script file name!");
            }

            XQueryExecutable exp = comp.compile(queryReader);
            XQueryEvaluator ev = exp.load();
            ev.setExternalVariable(new QName("source_url"), new XdmAtomicValue(script.getSrcFileUrl()));
            //ev.setExternalVariable(new QName("base_url"), new XdmAtomicValue("http://" + Properties.appHost + Properties.contextPath));
            XdmValue val = ev.evaluate();
            proc.writeXdmValue(val, out);
        } catch (SaxonApiException e) {
            LOGGER.debug("Error in XQuery script: " + e.getMessage());
            throw new ScriptExecutionException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            LOGGER.error("XQuery script file not found: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("IO Error while reading script: " + e.getMessage());
        } finally {
            if (queryReader != null) {
                try {
                    queryReader.close();
                } catch (IOException e) {
                    LOGGER.error("Error while attempting to close reader: " + e.getMessage());
                }
            }
        }
    }
}
