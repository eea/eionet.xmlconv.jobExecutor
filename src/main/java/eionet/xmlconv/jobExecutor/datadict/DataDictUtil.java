package eionet.xmlconv.jobExecutor.datadict;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import org.apache.commons.lang3.Conversion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDictUtil {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDictUtil.class);

    public static final String INSTANCE_SERVLET = "GetXmlInstance";
    public static final String SCHEMA_SERVLET = "GetSchema";
    public static final String CONTAINER_SCHEMA_SERVLET = "GetContainerSchema";

    /**
     * Retreive dataset released information from Data Dictionary for XML schema. If it is not DD schema, then return null
     *
     * @param xmlSchema XML Schema
     * @return Dataset released information for xml schema
     */
    public static Map<String, String> getDatasetReleaseInfoForSchema(String xmlSchema) {

        Map<String, String> dataset = null;

        if (xmlSchema == null || !xmlSchema.startsWith(Properties.ddURL)) {
            return dataset;
        }

        if (xmlSchema.contains("/schema-dst-")) {
            String id = StringUtils.substringBefore(StringUtils.substringAfter(xmlSchema, "/schema-dst-"), ".");
            dataset = getDatasetReleaseInfo("dst", id);
        } else if (xmlSchema.contains("/schema-tbl-")) {
            String id = StringUtils.substringBefore(StringUtils.substringAfter(xmlSchema, "/schema-tbl-"), ".");
            dataset = getDatasetReleaseInfo("tbl", id);
        } else {
            String id = getSchemaIdParamFromUrl(xmlSchema);

            if (id.length() > 4 && (id.startsWith(DD_XMLInstance.DST_TYPE) || id.startsWith(DD_XMLInstance.TBL_TYPE))) {

                String type = id.substring(0, 3);
                String dsId = id.substring(3);
                dataset = getDatasetReleaseInfo(type.toLowerCase(), dsId);
            }
        }
        return dataset;

    }
    /**
     * Retreive dataset released information from Data Dictionary for given ID and type If it is not DD schema, then return null
     *
     * @param type Dataset type.
     * @param dsId Dataset ID.
     * @return Dataset release information.
     */
    public static Map<String, String> getDatasetReleaseInfo(String type, String dsId) {
        //TODO endpoint
        return null;
    }
    /**
     * Extract id parameter value from URL if available, otherwise return empty String.
     *
     * @param schemaUrl Schema URL
     * @return ID
     */
    public static String getSchemaIdParamFromUrl(String schemaUrl) {

        String id = "";
        int id_idx = schemaUrl.indexOf("id=");

        if (id_idx > -1) {
            id = schemaUrl.substring(id_idx + 3);
        }
        if (id.indexOf("&") > -1) {
            id = id.substring(0, id.indexOf("&"));
        }

        return id;
    }

    /**
     * gather all element definitions
     *
     * @param schemaUrl Schema URL
     */
    public static Map<String, DDElement> importDDTableSchemaElemDefs(String schemaUrl) {
        //TODO
        return null;
    }

    /**
     * Constructs instance file URL using schema ID.
     * @param schema_url Schema URL
     * @return Instance URL
     * @throws ConversionException In case of unexpected error.
     */
    public static String getInstanceUrl(String schema_url) throws ConversionException {

        try {
            if (schema_url != null) {
                if (schema_url.toLowerCase().contains("/getschema")) {
                    // throws Exception, if not correct URL
                    URL schemaURL = new URL(schema_url);

                    String id = getSchemaIdParamFromUrl(schema_url);

                    String type = id.substring(0, 3);
                    id = id.substring(3);

                    int path_idx = schema_url.toLowerCase().indexOf(SCHEMA_SERVLET.toLowerCase());
                    String path = schema_url.substring(0, path_idx);

                    String instance_url = path + INSTANCE_SERVLET + "?id=" + id + "&type=" + type.toLowerCase();

                    // throws Exception, if not correct URL
                    URL instanceURL = new URL(instance_url);
                    return instance_url;
                } else if (schema_url.contains("/schema-tbl-")) {
                    int index = schema_url.lastIndexOf("/");
                    String id = StringUtils.substringBefore(StringUtils.substringAfter(schema_url, "/schema-tbl-"), ".");
                    String instanceURL = schema_url.substring(0, index) + "/table-" + id + "-instance.xml";
                    URL x = new URL(instanceURL);
                    return instanceURL;
                } else if (schema_url.contains("/schema-dst-")) {
                    int index = schema_url.lastIndexOf("/");
                    String instanceURL = "";
                    if (index > -1) {
                        instanceURL = schema_url.substring(0, index) + "/dataset-instance.xml";
                    }
                    URL x = new URL(instanceURL);
                    return instanceURL;
                }
            }
            throw new MalformedURLException("Could not parse URL");
        } catch (MalformedURLException e) {
            throw new ConversionException("Error getting Instance file URL: " + e.toString() + " - " + schema_url);
        } catch (Exception e) {
            throw new ConversionException("Error getting Instance file URL: " + e.toString() + " - " + schema_url);
        }
    }

}
