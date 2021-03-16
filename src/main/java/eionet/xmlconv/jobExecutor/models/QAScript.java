package eionet.xmlconv.jobExecutor.models;

import java.io.Serializable;

public class QAScript implements Serializable {

    String scriptId;
    String description;
    String shortName;
    String fileName;
    String schemaId;
    String schema;
    String resultType;
    String scriptType;
    String modified;
    String checksum;
    String scriptContent;
    String upperLimit;
    String url;
    boolean active;
    boolean asynchronousExecution;

    private boolean blocker = false;

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    String filePath;

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Default constructor
     */
    public QAScript() {
        super();
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String queryId) {
        this.scriptId = queryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String queryType) {
        this.scriptType = queryType;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setActive(String isActive) {
        this.active = isActive.equals("1");
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * @return the blocker
     */
    public boolean isBlocker() {
        return blocker;
    }

    /**
     * @param blocker the blocker to set
     */
    public void setBlocker(boolean blocker) {
        this.blocker = blocker;
    }

    public boolean isAsynchronousExecution() {
        return asynchronousExecution;
    }

    public void setAsynchronousExecution(boolean asynchronousExecution) {
        this.asynchronousExecution = asynchronousExecution;
    }

}
