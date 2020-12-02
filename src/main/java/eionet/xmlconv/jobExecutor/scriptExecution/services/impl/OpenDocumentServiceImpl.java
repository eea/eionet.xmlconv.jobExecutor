package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.converters.ConvertStrategy;
import eionet.xmlconv.jobExecutor.converters.XMLConverter;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ConvertContextService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.OpenDocumentService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.readers.OdsReaderServiceImpl;
import eionet.xmlconv.jobExecutor.utils.StreamsUtils;
import eionet.xmlconv.jobExecutor.utils.Utils;
import eionet.xmlconv.jobExecutor.utils.ZipUtils;
import eionet.xmlconv.jobExecutor.utils.tiny.TinyTreeContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Service
public class OpenDocumentServiceImpl implements OpenDocumentService {
    public static final String ODS_TEMPLATE_FILE_NAME = "template.ods";
    public static final String META_FILE_NAME = "meta.xml";
    public static final String METAXSL_FILE_NAME = "meta.xsl";
    public static final String CONTENT_FILE_NAME = "content.xml";

    public static final Logger LOGGER = LoggerFactory.getLogger(OpenDocumentService.class);

    private String strWorkingFolder = null;
    private String strMetaFile = null;
    private String strMetaXslFile = null;
    private String strOdsTemplateFile = null;
    private String strOdsOutFile = null;
    private String strContentFile = null;

    /**
     * Default constructor
     */
    @Autowired
    public OpenDocumentServiceImpl() {

    }

    @Override
    public void setContentFile(String strContentFile) {
        this.strContentFile = strContentFile;
    }

    /**
     * Creates ODS file.
     * @param strOut Output String
     * @throws Exception If an error occurs.
     */
    @Override
    public void createOdsFile(String strOut) throws Exception {

        FileOutputStream resultFileOutput = new FileOutputStream(strOut);

        try {
            createOdsFile(resultFileOutput);
        } finally {
            IOUtils.closeQuietly(resultFileOutput);
        }

    }

    /**
     * Method unzips the ods file, replaces content.xml and meta.xml and finally zips it together again
     * @param out OutputStream
     * @throws Exception If an error occurs.
     */
    @Override
    public void createOdsFile(OutputStream out) throws Exception {

        if (strContentFile == null) {
            throw new Exception("Content file is not set!");
        }

        initOdsFiles();

        FileInputStream result_file_input = null;
        FileOutputStream zip_file_output = new FileOutputStream(strOdsOutFile);
        ZipOutputStream zip_out = new ZipOutputStream(zip_file_output);

        try {
            // unzip template ods file to temp directory
            ZipUtils.unzip(strOdsTemplateFile, strWorkingFolder);
            // copy conent file into temp directory
            Utils.copyFile(new File(strContentFile), new File(strWorkingFolder + File.separator + CONTENT_FILE_NAME));
            // try to transform meta with XSL, if it fails then copy meta file into temp directory
            try {
                convertMetaFile();
            } catch (Throwable t) {
                Utils.copyFile(new File(strMetaFile), new File(strWorkingFolder + File.separator + META_FILE_NAME));
            }
            // zip temp directory
            ZipUtils.zipDir(strWorkingFolder, zip_out);
            zip_out.finish();
            zip_out.close();

            // Fill outputstream
            result_file_input = new FileInputStream(strOdsOutFile);
            StreamsUtils.drain(result_file_input, out);

        } catch (IOException ioe) {
            throw new Exception("Could not create OpenDocument Spreadsheet file: " + ioe.toString());
        } finally {
            IOUtils.closeQuietly(zip_out);
            IOUtils.closeQuietly(zip_file_output);
            IOUtils.closeQuietly(result_file_input);
        }
        try {
            // delete working folder and temporary ods file
            Utils.deleteFolder(strWorkingFolder);
            Utils.deleteFile(strOdsOutFile);
        } catch (Exception ioe) {
            LOGGER.error(("Couldn't delete temp files."));
        }

    }

    /**
     * Prepares working folder.
     * @throws Exception If an error occurs.
     */
    private void prepareWorkingFolder() throws Exception {

        // get temporary folder
        String tmpFilePath = Properties.tmpFolder;
        if (tmpFilePath == null) {
            throw new Exception("Missing property: tmp.folder");
        } else if (!tmpFilePath.endsWith(File.separator)) {
            tmpFilePath = new File(tmpFilePath).getAbsolutePath() + File.separator;
        }

        // build working folder name
        StringBuffer buf = new StringBuffer(tmpFilePath);
        buf.append("ods_");
        buf.append(Utils.getRandomName());

        // create working folder
        File workginFolder = new File(buf.toString());
        workginFolder.mkdir();

        strWorkingFolder = workginFolder.getAbsolutePath();
    }

    /**
     * ODS files initialization.
     * @throws Exception If an error occurs.
     */
    private void initOdsFiles() throws Exception {

        prepareWorkingFolder();
        if (strWorkingFolder == null) {
            throw new Exception("Working folder is not created!");
        }

        // get ods-folder path
        String odsFolder = Properties.odsFolder;
        if (odsFolder == null) {
            throw new Exception("Missing property: ods.folder");
        } else if (!odsFolder.endsWith(File.separator)) {
            odsFolder = new File(odsFolder).getAbsolutePath() + File.separator;
        }

        String tmpFilePath = Properties.tmpFolder;
        if (tmpFilePath == null) {
            throw new Exception("Missing property: tmp.folder");
        } else if (!tmpFilePath.endsWith(File.separator)) {
            tmpFilePath = new File(tmpFilePath).getAbsolutePath() + File.separator;
        }

        strOdsOutFile = tmpFilePath + "gdem_out" + System.currentTimeMillis() + ".ods";
        strOdsTemplateFile = odsFolder + ODS_TEMPLATE_FILE_NAME;
        strMetaFile = odsFolder + META_FILE_NAME;
        strMetaXslFile = odsFolder + METAXSL_FILE_NAME;
    }

    /**
     * Finds schema-url attributes from content file (stored in xsl) table:table attribute and transforms the values into meta.xml
     * file user defined properties
     * @throws Exception If an error occurs.
     */
    private void convertMetaFile() throws Exception {
        String schemaUrl = null;
        FileOutputStream os = null;
        FileInputStream in = null;
        StringBuffer tableSchemaUrls = new StringBuffer();

        try {
            TinyTreeContext ctx = new TinyTreeContext();
            ctx.setFile(strContentFile);
            TinyTreeXpathServiceImpl xQuery = ctx.getQueryManager();
            xQuery.declareNamespace("table", "http://openoffice.org/2000/table");
            List elements = xQuery.getElementAttributes("table:table");
            for (int i = 0; i < elements.size(); i++) {
                HashMap attr_map = (HashMap) elements.get(i);
                if (attr_map.containsKey(OdsReaderServiceImpl.SCHEMA_ATTR_NAME) && Utils.isNullStr(schemaUrl)) {
                    schemaUrl = (String) attr_map.get(OdsReaderServiceImpl.SCHEMA_ATTR_NAME);
                }
                if (attr_map.containsKey(OdsReaderServiceImpl.TBL_SCHEMAS_ATTR_NAME)) {
                    if (attr_map.containsKey("table:name")) {
                        String schema_url = (String) attr_map.get(OdsReaderServiceImpl.TBL_SCHEMAS_ATTR_NAME);
                        String name = (String) attr_map.get("table:name");
                        if (!Utils.isNullStr(schema_url) && !Utils.isNullStr(name)) {
                            tableSchemaUrls.append(OdsReaderServiceImpl.TABLE_NAME);
                            tableSchemaUrls.append(name);
                            tableSchemaUrls.append(";");
                            tableSchemaUrls.append(OdsReaderServiceImpl.TABLE_SCHEMA_URL);
                            tableSchemaUrls.append(schema_url);
                            tableSchemaUrls.append(";");
                        }
                    }
                }
            }
            if (!Utils.isNullStr(schemaUrl)) {
                os = new FileOutputStream(strWorkingFolder + File.separator + META_FILE_NAME);
                in = new FileInputStream(strMetaFile);
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put(OdsReaderServiceImpl.SCHEMA_ATTR_NAME, schemaUrl);
                parameters.put(OdsReaderServiceImpl.TBL_SCHEMAS_ATTR_NAME, tableSchemaUrls.toString());
                ConvertContextService conversionContext = new ConvertContextServiceImpl(in, strMetaXslFile, os, "xml");
                ConvertStrategy cs = new XMLConverter();
                cs.setXslParams(parameters);
                conversionContext.executeConversion(cs);

                // XSLTransformer transform = new XSLTransformer();
                // transform.transform(strMetaXslFile, new InputSource(in), os, parameters);
            }

        } catch (Exception ex) {
            LOGGER.error("Error converting meta.xml");
            throw ex;
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(in);
        }
    }
}
