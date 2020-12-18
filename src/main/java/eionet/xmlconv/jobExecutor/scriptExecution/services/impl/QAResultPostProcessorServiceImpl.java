package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.datadict.DataDictUtil;
import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;
import eionet.xmlconv.jobExecutor.exceptions.XmlconvApiException;
import eionet.xmlconv.jobExecutor.models.Schema;
import eionet.xmlconv.jobExecutor.scriptExecution.services.QAResultPostProcessorService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DataRetrieverService;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import eionet.xmlconv.jobExecutor.scriptExecution.services.XmlHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QAResultPostProcessorServiceImpl implements QAResultPostProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAResultPostProcessorService.class);
    @Autowired
    private DataRetrieverService dataRetrieverService;
    private String warnMessage;

    @Autowired
    public QAResultPostProcessorServiceImpl() {
    }

    /**
     * Checks if the QA was made against expired schema. Adds a warning on top of the QA result if the result is HTML format.
     * @param result QA result
     * @param xmlSchema XML Schema
     * @return Processed result
     * @throws ScriptExecutionException If an error occurs.
     */
    @Override
    public String processQAResult(String result, Schema xmlSchema) throws ScriptExecutionException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.warnMessage = getWarningMessage(xmlSchema);
        if (warnMessage != null) {
            XmlHandlerService vdt = new VtdHandlerServiceImpl();
            vdt.addWarningMessage(result, warnMessage, out);
        } else {
            try {
                out.write(result.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new ScriptExecutionException("Couldn't write to OutputStream: " + e.getMessage());
            }
        }

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Process QA result
     * @param result Result
     * @param xmlSchemaUrl Schema URL
     * @return Processed result
     * @throws ScriptExecutionException If an error occurs.
     */
    @Override
    public String processQAResult(String result, String xmlSchemaUrl) throws ScriptExecutionException {

        Schema schema = getSchemaObject(xmlSchemaUrl);
        return processQAResult(result, schema);
    }

    /**
     * Returns warning message for given schema URL.
     *
     * @param xmlSchemaUrl XML Schema URL
     * @return Warning message
     */
    @Override
    public String getWarningMessage(String xmlSchemaUrl) {

        if (warnMessage != null) {
            return warnMessage;
        }
        Schema schema = getSchemaObject(xmlSchemaUrl);
        return getWarningMessage(schema);
    }

    /**
     * Returns warning message if schema is expired.
     *
     * @param xmlSchema XML Schema
     * @return Warning message
     */
    private String getWarningMessage(Schema xmlSchema) {

        String localSchemaExpiredMessage = getLocalSchemaExpiredMessage(xmlSchema);
        if (localSchemaExpiredMessage != null) {
            return localSchemaExpiredMessage;
        }

        String ddSchemaExpiredMessage = getDDSchemaExpiredMessage(xmlSchema);
        if (ddSchemaExpiredMessage != null) {
            return ddSchemaExpiredMessage;
        }

        return null;
    }

    /**
     * Get Schema object from database
     *
     * @param xmlSchemaUrl XML Schema URL
     * @return Schema object
     */
    private Schema getSchemaObject(String xmlSchemaUrl) {

        Schema schema = null;
        String schemaId;
        try {
            schema = dataRetrieverService.retrieveSchemaByXmlUrl(xmlSchemaUrl);
        } catch (XmlconvApiException e) {
            LOGGER.error("Unable to find Schema information from database" + e.toString());
            e.printStackTrace();
        }

        if (schema == null && xmlSchemaUrl != null) {
            schema = new Schema();
            schema.setSchema(xmlSchemaUrl);
        }
        return schema;
    }

    /**
     * Check if given XML Schema is marked as expired in XMLCONV repository. Returns error message, otherwise null.
     *
     * @param xmlSchema XML Schema
     * @return Schema Expired message
     */
    private String getLocalSchemaExpiredMessage(Schema xmlSchema) {

        if (xmlSchema != null && xmlSchema.isExpired()) {

            // schema is expired add message in top of the QA result
            String expDate = Utils.getFormat(xmlSchema.getExpireDate(), "dd.MM.yyyy");
            String message = Properties.getMessage(Constants.WARNING_QA_EXPIRED_SCHEMA, new String[] {expDate});
            return message;
        }
        return null;
    }

    /**
     * Check if schema is the latest released version in DD (in case of DD schema). If it is not latest released then return warning
     * message.
     *
     * @param xmlSchema XML Schema
     * @return Schema expired message.
     */
    private String getDDSchemaExpiredMessage(Schema xmlSchema) {

        Map<String, String> dataset = getDataset(xmlSchema.getSchema());
        if (dataset != null) {
            String status = dataset.get("status");
            boolean isLatestReleased =
                    (dataset.get("isLatestReleased") == null || "true".equals(dataset.get("isLatestReleased"))) ? true
                            : false;
            String dateOfLatestReleased = dataset.get("dateOfLatestReleased");
            String idOfLatestReleased = dataset.get("idOfLatestReleased");

            if (!isLatestReleased && "Released".equalsIgnoreCase(status)) {
                String formattedReleasedDate = Utils.formatTimestampDate(dateOfLatestReleased);
                String message =
                        Properties.getMessage(Constants.WARNING_QA_EXPIRED_DD_SCHEMA, new String[] {
                                formattedReleasedDate == null ? "" : formattedReleasedDate, idOfLatestReleased});
                return message;
            }
        }
        return null;
    }

    /**
     * Get DD XML Schema released info
     *
     * @param xmlSchema XML Schema
     * @return Dataset Map
     */
    protected Map<String, String> getDataset(String xmlSchema) {
        return DataDictUtil.getDatasetReleaseInfoForSchema(xmlSchema);
    }
}
