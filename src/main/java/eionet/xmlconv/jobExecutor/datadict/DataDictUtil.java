package eionet.xmlconv.jobExecutor.datadict;

import eionet.xmlconv.jobExecutor.Properties;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DataDictUtil {
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

}
