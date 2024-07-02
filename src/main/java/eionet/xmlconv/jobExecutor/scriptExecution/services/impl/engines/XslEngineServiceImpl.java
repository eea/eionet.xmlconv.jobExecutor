package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.engines;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.converters.ConvertStrategy;
import eionet.xmlconv.jobExecutor.converters.XMLConverter;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.models.Script;
import eionet.xmlconv.jobExecutor.rabbitmq.model.WorkerJobInfoRabbitMQResponseMessage;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ConvertContextService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.HttpFileManagerService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.ConvertContextServiceImpl;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.HttpFileManagerServiceImpl;
import eionet.xmlconv.jobExecutor.utils.UrlUtils;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Service("xslEngineService")
public class XslEngineServiceImpl extends ScriptEngineServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(XslEngineServiceImpl.class);

    @Autowired
    public XslEngineServiceImpl() {
    }

    @Override
    protected void runQuery(Script script, OutputStream result, WorkerJobInfoRabbitMQResponseMessage response) throws ScriptExecutionException {

        FileInputStream fisXsl = null;
        String tmpXslFile = null;
        InputStream sourceStream = null;
        HttpFileManagerService fileManager = new HttpFileManagerServiceImpl();
        try {
            // build InputSource for xsl
            if (!Utils.isNullStr(script.getScriptSource())) {
                tmpXslFile = Utils.saveStrToFile(null, script.getScriptSource(), "xsl");
            } else if (!Utils.isNullStr(script.getScriptFileName())) {
                fisXsl = new FileInputStream(script.getScriptFileName());
            } else {
                throw new ScriptExecutionException("XQuery engine could not find script source or script file name!");
            }
            // Build InputSource for xml file
            sourceStream = fileManager.getFileInputStream(script.getSrcFileUrl(), null, false);
            // execute xsl transformation

            ConvertContextService ctx =
                    new ConvertContextServiceImpl(sourceStream, tmpXslFile == null ? script.getScriptFileName() : tmpXslFile,
                            result, null);
            ConvertStrategy cs = new XMLConverter();

            Map<String, String> params = UrlUtils.getCdrParams(script.getSrcFileUrl());
            params.put(Constants.XQ_SOURCE_PARAM_NAME, script.getOrigFileUrl());
            cs.setXslParams(params);
            ctx.executeConversion(cs);

            if (tmpXslFile != null) {
                Utils.deleteFile(tmpXslFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("==== Caught EXCEPTION " + e.toString());
            throw new ScriptExecutionException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(sourceStream);
            fileManager.closeQuietly();
            IOUtils.closeQuietly(fisXsl);
        }

    }

}
