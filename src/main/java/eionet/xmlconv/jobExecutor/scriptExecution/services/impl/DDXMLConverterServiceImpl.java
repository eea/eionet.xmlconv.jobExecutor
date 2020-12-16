package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.datadict.DDElement;
import eionet.xmlconv.jobExecutor.datadict.DD_XMLInstance;
import eionet.xmlconv.jobExecutor.datadict.DataDictUtil;
import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.models.ConversionLogDto;
import eionet.xmlconv.jobExecutor.models.ConversionResultDto;
import eionet.xmlconv.jobExecutor.scriptExecution.services.DDXMLConverterService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.utils.OpenDocumentUtils;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Abstract class contains the logic for converting spreadsheet like datafiles into DataDictionary XML Instance format. The
 * spreadsheets should be extracted from DD and include XML Schema information. Currently supported formats are MS Excel and
 * OpenDocument Spreadsheet.
 */
@Service
public abstract class DDXMLConverterServiceImpl implements DDXMLConverterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DDXMLConverterServiceImpl.class);

    public static final String META_SHEET_NAME = "-meta";
    public static final String META_SHEET_NAME_ODS = "_meta";

    protected SourceReaderService sourcefile = null;
    private boolean httpResponse = false;
    protected String xmlSchema = null;
    private boolean isInitialized = false;
    private boolean isValidSchema = false;
    private boolean isValidSheetSchemas = false;
    protected ConversionResultDto resultObject= null;
    protected Map<String, String> sheetSchemas = null;

    /**
     * Default constructor.
     */
    @Autowired
    DDXMLConverterServiceImpl() {
    }

    /**
     * Returns Reader
     * @return Reader
     */
    public abstract SourceReaderService getSourceReader();

    /**
     * Gets Source format name
     * @return Source format name
     */
    public abstract String getSourceFormatName();

    /**
     * Gets converter
     * @param inFile Input File
     * @param resultObject Result object
     * @param sheetParam Sheet parameters
     * @return Converter
     * @throws ConversionException If an error occurs.
     */
    @Override
    public DDXMLConverterService getConverter(File inFile, ConversionResultDto resultObject, String sheetParam) throws ConversionException {
        DDXMLConverterServiceImpl converter = null;
        try {
            converter = new Excel2XMLServiceImpl();
            converter.initConverter(inFile);
            LOGGER.debug("Excel 2003 or older document");
        } catch (Exception e) {
            LOGGER.debug("Excel 2003 or older document failed: " + e.getMessage());
        }
        if (!converter.isInitialized()) {
            try {
                converter = new Excel20072XMLServiceImpl();
                converter.initConverter(inFile);
                LOGGER.debug("Excel 2007 document");
            } catch (Exception e) {
                LOGGER.debug("Excel 2007 document failed: " + e.getMessage());
            }
        }

        if (!converter.isInitialized()) {
            // If it is a zip file, then it is OpenDocument
            try {
                if (OpenDocumentUtils.isSpreadsheetFile(new FileInputStream(inFile))) {
                    converter = new Ods2XmlServiceImpl();
                    converter.initConverter(inFile);
                    LOGGER.debug("OpenDocument spreadsheet");
                }
            } catch (Exception e) {
                LOGGER.debug("OpenDocument spreadsheet failed", e);
            }
        }
        if (converter == null || !converter.isInitialized()) {
            LOGGER.error("Could not detect the format of source file. "
                    + "Converter waits MS Excel or OpenDocument Spreadsheet file.");
            throw new ConversionException(
                    "Could not detect the format of source file. Converter waits MS Excel or OpenDocument Spreadsheet file.");
        }
        converter.startConverter(resultObject, sheetParam);
        return converter;
    }

    /**
     * Initializes converter
     * @param inFile Input file
     * @throws ConversionException If an error occurs.
     */
    @Override
    public void initConverter(File inFile) throws ConversionException {
        sourcefile = getSourceReader();
        sourcefile.initReader(inFile);
        setInitialized(true);
    }

    /**
     * Starts converter
     * @param resultObject Result Object
     * @param sheetParam Sheet parameter
     * @throws ConversionException If an error occurs.
     */
    @Override
    public void startConverter(ConversionResultDto resultObject, String sheetParam) throws ConversionException {
        this.resultObject = resultObject;
        sourcefile.startReader(resultObject);
        this.xmlSchema = sourcefile.getXMLSchema();
        this.isValidSchema = isValidXmlSchema(xmlSchema);
        this.sheetSchemas = sourcefile.getSheetSchemas();
        this.isValidSheetSchemas = isValidSheetSchemas(sheetSchemas, xmlSchema, sheetParam);
    }

    /**
     * Gets result transfer object
     * @param outStream OutputStream
     * @return Converted XML
     * @throws ConversionException If an error occurs.
     */
    @Override
    public ConversionResultDto convertDD_XML(OutputStream outStream) throws ConversionException {

        try {
            if (outStream == null) {
                throw new Exception("Could not find OutputStream");
            }
            doConversion(xmlSchema, outStream);
            parseConversionResults();
            sourcefile.closeReader();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConversionException("Error generating XML file from " + getSourceFormatName() + " file: " + e.toString(), e);
        }
        return resultObject;
    }

    /**
     * Gets result transfer object
     * @param outStream OutputStream
     * @param sheetParam Sheet parameters
     * @return Converted XML
     * @throws ConversionException If an error occurs.
     */
    @Override
    public ConversionResultDto convertDD_XML_split(OutputStream outStream, String sheetParam)
            throws ConversionException {
        try {
            if (isHttpResponse() && Utils.isNullStr(sheetParam)) {
                sheetParam = sourcefile.getFirstSheetName();
            }
            if (sheetParam != null && sheetParam.length() > 31) {
                sheetParam = sheetParam.substring(0,31);
            }
            for (Map.Entry<String, String> entry : sheetSchemas.entrySet()) {
                String sheetName = entry.getKey();
                String sheetSchema = entry.getValue();
                if (sheetSchema == null) {
                    resultObject.addConversionLog(ConversionLogDto.ConversionLogType.WARNING, "could not find xml schema for this sheet!",
                            ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
                    continue;
                }
                if (!Utils.isNullStr(sheetParam)) {
                    // Only 1 sheet is needed.
                    if (!sheetParam.equalsIgnoreCase(sheetName)) {
                        continue;
                    }
                }

                try {
                    // Do not return empty sheets.
                    if (sourcefile.isEmptySheet(sheetName)) {
                        resultObject.addConversionLog(ConversionLogDto.ConversionLogType.INFO, "The sheet is empty: " + sheetName,
                                ConversionLogDto.CATEGORY_SHEET + ": " + sheetName);
                        continue;
                    }
                    String tmpFileName = Utils.getUniqueTmpFileName(".xml");
                    if (!isHttpResponse() && outStream == null) {
                        outStream = new FileOutputStream(tmpFileName);
                    }
                    doConversion(sheetSchema, outStream);
                    // if the respponse is http stream, then it is already
                    // written there and no file available
                    if (!isHttpResponse()) {
                        //resultObject.addConvertedXml(sheetName + ".xml", ((ByteArrayOutputStream) outStream).toByteArray());
                        resultObject.addConvertedFile(sheetName + ".xml", tmpFileName);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    resultObject.addConversionLog(ConversionLogDto.ConversionLogType.ERROR,
                            "System error occourd during converting this sheet " + sheetName, ConversionLogDto.CATEGORY_SHEET + ": "
                                    + sheetName);
                } finally {
                    if (!isHttpResponse()) {
                        IOUtils.closeQuietly(outStream);
                        outStream = null;
                    }
                }
                if (!Utils.isNullStr(sheetParam)) {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ConversionException("Error generating XML files from " + getSourceFormatName() + " file: " + e.toString(), e);
        }
        sourcefile.closeReader();
        parseConversionResults();
        return resultObject;
    }

    public boolean isHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(boolean httpResponse) {
        this.httpResponse = httpResponse;
    }

    /**
     * Converts XML file
     * @param xmlSchema XML schema
     * @param outStream OutputStream
     * @throws Exception If an error occurs.
     */
    protected void doConversion(String xmlSchema, OutputStream outStream) throws Exception {
        String instanceUrl = DataDictUtil.getInstanceUrl(xmlSchema);

        DD_XMLInstance instance = new DD_XMLInstance(instanceUrl);
        DD_XMLInstanceServiceImpl handler = new DD_XMLInstanceServiceImpl(instance);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        factory.setValidating(false);
        factory.setNamespaceAware(true);
        reader.setFeature("http://xml.org/sax/features/validation", false);
        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);

        reader.setContentHandler(handler);
        reader.parse(instanceUrl);

        if (Utils.isNullStr(instance.getEncoding())) {
            String enc_url = Utils.getEncodingFromStream(instanceUrl);
            if (!Utils.isNullStr(enc_url)) {
                instance.setEncoding(enc_url);
            }
        }
        importSheetSchemas(sourcefile, instance, xmlSchema);
        instance.startWritingXml(outStream);
        sourcefile.writeContentToInstance(instance);
        instance.flushXml();
    }



    /**
     * Gather all element definitions
     *
     * @param spreadsheet Spreadsheet
     * @param instance XML Instance
     * @param xmlSchema XML Schema
     */
    protected void importSheetSchemas(SourceReaderService spreadsheet, DD_XMLInstance instance, String xmlSchema) {
        try {
            // if instance type is TBL, then import only table schema
            if (instance.getType().equals(DD_XMLInstance.TBL_TYPE)) {
                Map<String, DDElement> elemDefs = DataDictUtil.importDDTableSchemaElemDefs(xmlSchema);
                instance.addElemDef(DD_XMLInstance.TBL_TYPE, elemDefs);
            }
            // if instance type is dataset, then import schemas for all pages
            else {
                Map<String, String> sheetSchemas = spreadsheet.getSheetSchemas();
                for (Map.Entry<String, String> entry : sheetSchemas.entrySet()) {
                    String sheetName = entry.getKey();
                    String schemaUrl = entry.getValue();
                    Map<String, DDElement> elemDefs = DataDictUtil.importDDTableSchemaElemDefs(schemaUrl);
                    instance.addElemDef(sheetName, elemDefs);
                }
            }
        } catch (Exception ex) {
            String errMess = "Unable to read element definitions from Data Dictionary XML Schema: " + xmlSchema;
            LOGGER.error(errMess, ex);
            ex.printStackTrace();
            resultObject.addConversionLog(ConversionLogDto.ConversionLogType.WARNING, errMess, "Workbook");
        }
    }

    /**
     * Checks if the given schema belongs to the last released dataset in DD. Returns null, if schema is OK. Returns an error
     * message, if the schema is not ok to convert.
     *
     * @param xmlSchema XML Schema
     * @return Invalid Schema error message
     * @throws ConversionException If an error occurs
     */
    @Override
    public String getInvalidSchemaMessage(String xmlSchema) throws ConversionException {

        String result = null;

        // check latest version only if it Schema from DD
        if (xmlSchema != null && xmlSchema.startsWith(Properties.ddURL)) {
            Map<String, String> dataset = getDataset(xmlSchema);
            if (dataset == null) {
                result =
                        Properties.getMessage(Constants.ERROR_CONVERSION_INVALID_TEMPLATE,
                                new String[] {getSourceFormatName()});
            } else {
                String status = dataset.get("status");
                boolean isLatestReleased =
                        (dataset.get("isLatestReleased") == null || "true".equals(dataset.get("isLatestReleased"))) ? true : false;
                String dateOfLatestReleased = dataset.get("dateOfLatestReleased");
                String idOfLatestReleased = dataset.get("idOfLatestReleased");

                if (!isLatestReleased && "Released".equalsIgnoreCase(status)) {
                    String formattedReleasedDate = Utils.formatTimestampDate(dateOfLatestReleased);
                    result =
                            Properties.getMessage(Constants.ERROR_CONVERSION_OBSOLETE_TEMPLATE, new String[] {
                                    getSourceFormatName(), formattedReleasedDate == null ? "" : formattedReleasedDate,
                                    idOfLatestReleased});
                }
            }
        }

        return result;
    }

    /**
     * Gets dataset info for the specific schema
     * @param xmlSchema XML Schema
     * @return Dataset info
     */
    protected Map<String, String> getDataset(String xmlSchema) {
        return DataDictUtil.getDatasetReleaseInfoForSchema(xmlSchema);
    }

    /**
     * Checks if XML conforms to a valid schema
     * @param xmlSchema XML Schema
     * @return True if schema is valid.
     * @throws ConversionException If an error occurs.
     */
    private boolean isValidXmlSchema(String xmlSchema) throws ConversionException {
        boolean isValidXmlSchema = true;
        String invalidMess = null;
        if (xmlSchema == null) {
            isValidXmlSchema = false;
            invalidMess =
                    Properties.getMessage(Constants.ERROR_CONVERSION_INVALID_TEMPLATE,
                            new String[] {getSourceFormatName()});
        } else {
            invalidMess = getInvalidSchemaMessage(xmlSchema);
            if (invalidMess != null) {
                isValidXmlSchema = false;
            }
        }
        if (!isValidXmlSchema) {
            resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_SCHEMA_NOT_FOUND);
            resultObject.setStatusDescription(invalidMess);
        }
        return isValidXmlSchema;
    }

    /**
     * Checks if sheet schemas are valid.
     * @param sheetSchemas Sheet schemas
     * @param xmlSchema XML Schema
     * @param sheetName Sheet name
     * @return True if sheet schemas are valid.
     */
    private boolean isValidSheetSchemas(Map<String, String> sheetSchemas, String xmlSchema, String sheetName) {
        boolean isValidSheetSchema = true;

        // could not find sheet schemas
        if (Utils.isNullHashMap(sheetSchemas)) {
            // maybe it's spreadsheet file for DD table
            if (xmlSchema.toLowerCase().indexOf("type=tbl") > -1 || xmlSchema.toLowerCase().indexOf("=tbl") > -1) {
                sheetSchemas.put(sourcefile.getFirstSheetName(), xmlSchema);
            } else {
                isValidSheetSchema = false;
                resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_SCHEMA_NOT_FOUND);
                resultObject.setStatusDescription(Properties.getMessage(Constants.ERROR_CONVERSION_INVALID_TEMPLATE,
                        new String[] {getSourceFormatName()}));
            }
        }
        if (!Utils.isNullStr(sheetName)) {
            if (sheetName.length() > 31) {
                sheetName = sheetName.substring(0, 31);
            }
            if (!Utils.containsKeyIgnoreCase(sheetSchemas, sheetName)) {
                isValidSheetSchema = false;
                resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_SCHEMA_NOT_FOUND);
                resultObject
                        .setStatusDescription("Could not find sheet with specified name or the XML schema reference was missing on DO_NOT_DELETE_THIS_SHEET: "
                                + sheetName);
            }
        }
        return isValidSheetSchema;
    }

    /**
     * Parses conversion results
     */
    private void parseConversionResults() {
        if (resultObject.isContainsErrors()) {
            resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_SYSTEM);
            resultObject.setStatusDescription("Conversion contains errors.");
        } else if (resultObject.isContainsWarnings()) {
            resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_VALIDATION);
            resultObject.setStatusDescription("Conversion contains validation warnings.");
        } else {
            resultObject.setStatusCode(ConversionResultDto.STATUS_OK);
            resultObject.setStatusDescription("Conversion successful.");
        }
    }

    /**
     * @return the xmlSchema
     */
    public String getXmlSchema() {
        return xmlSchema;
    }

    /**
     * @return the isValidSchema
     */
    public boolean isValidSchema() {
        return isValidSchema;
    }

    /**
     * @return the isValidSheetSchemas
     */
    public boolean isValidSheetSchemas() {
        return isValidSheetSchemas;
    }
    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

}
