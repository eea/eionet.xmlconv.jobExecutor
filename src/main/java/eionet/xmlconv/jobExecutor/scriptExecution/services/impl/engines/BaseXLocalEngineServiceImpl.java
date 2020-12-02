package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.FollowRedirectException;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.scriptExecution.Script;
import eionet.xmlconv.jobExecutor.scriptExecution.services.HttpFileManagerService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.HttpFileManagerServiceImpl;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.basex.core.Context;
import org.basex.core.MainOptions;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.SerializerOptions;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import org.basex.core.cmd.Set;
import static java.util.Objects.isNull;

public class BaseXLocalEngineServiceImpl extends ScriptEngineServiceImpl{

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseXLocalEngineServiceImpl.class);

    @Override
    protected void runQuery(Script script, OutputStream result) throws ScriptExecutionException {

        Context context = new Context();
        QueryProcessor proc = null;
        try {
            new Set(MainOptions.INTPARSE, true).execute(context);
            new Set(MainOptions.MAINMEM, true).execute(context);

            String scriptSource = null;
            if (!Utils.isNullStr(script.getScriptSource())) {
                scriptSource = script.getScriptSource();
            } else if (!Utils.isNullStr(script.getScriptFileName())) {
                try (Reader queryReader = new FileReader(script.getScriptFileName())) {
                    scriptSource = new String(IOUtils.toByteArray(queryReader, "UTF-8"));
                } catch (IOException e) {
                    LOGGER.error("Error while reading XQuery file: " + e);
                    throw new ScriptExecutionException("Error while reading XQuery file: " + script.getScriptFileName() + " : " + ExceptionUtils.getStackTrace(e), e);
                }
            }
            proc = new QueryProcessor(scriptSource, Properties.queriesFolder + "/script", context);

            HttpFileManagerService fileManager = new HttpFileManagerServiceImpl();
            URL url = fileManager.followUrlRedirectIfNeeded(new URL(script.getSrcFileUrl()));
            script.setSrcFileUrl(url.toString());
            LOGGER.info("Script Source URL:"+script.getSrcFileUrl());
            proc.bind("source_url", script.getSrcFileUrl(), "xs:string");

//            proc.bind("base_url", Properties.gdemURL + Properties.contextPath , "xs:string");

            // same serialization options with saxon
            SerializerOptions opts = new SerializerOptions();

            opts.set(SerializerOptions.INDENT, "no");
            opts.set(SerializerOptions.ENCODING, DEFAULT_ENCODING);
            if (getOutputType().equals(HTML_CONTENT_TYPE)) {
                opts.set(SerializerOptions.METHOD, HTML_CONTENT_TYPE);
            } else {
                opts.set(SerializerOptions.METHOD, getOutputType());
            }

            if (getOutputType().equals(XML_CONTENT_TYPE)) {
                opts.set(SerializerOptions.OMIT_XML_DECLARATION, "no");
            } else {
                opts.set(SerializerOptions.OMIT_XML_DECLARATION, "yes");
            }

            Value res = proc.value();

            ArrayOutput A = res.serialize(opts);
            result.write(A.toArray());

            //logger.info("proc info: " + proc.info());
            //logger.info( new String(A.buffer() , "UTF-8" ));
        } catch (QueryException | IOException | FollowRedirectException e) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ScriptExecutionException(e.getMessage());
            } else {
                LOGGER.error("Error executing BaseX xquery script : " + e.getMessage());
                throw new ScriptExecutionException(e.getMessage(),e.getCause());
            }
        } finally {
            if (!isNull(proc))  {
                proc.close();
            }
            context.close();
        }
    }
}
