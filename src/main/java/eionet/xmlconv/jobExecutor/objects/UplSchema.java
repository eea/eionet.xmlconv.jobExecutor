package eionet.xmlconv.jobExecutor.objects;


import java.io.Serializable;

/**
 * Upload schema class.
 */
public class UplSchema implements Serializable {

    private String uplSchemaId;
    private String uplSchemaFile;
    private String uplSchemaFileUrl;
    private String schemaId;
    private String schemaUrl;
    private String description;
    private String lastModified;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Default constructor
     */
    public UplSchema() {

    }

    public String getUplSchemaId() {
        return uplSchemaId;
    }

    public void setUplSchemaId(String id) {
        this.uplSchemaId = id;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(String schema) {
        this.schemaUrl = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUplSchemaFile() {
        return uplSchemaFile;
    }

    public void setUplSchemaFile(String uplSchemaFile) {
        this.uplSchemaFile = uplSchemaFile;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getUplSchemaFileUrl() {
        return uplSchemaFileUrl;
    }

    public void setUplSchemaFileUrl(String uplSchemaFileUrl) {
        this.uplSchemaFileUrl = uplSchemaFileUrl;
    }

}
