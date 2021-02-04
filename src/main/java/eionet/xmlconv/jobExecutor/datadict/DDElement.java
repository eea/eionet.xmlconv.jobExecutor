package eionet.xmlconv.jobExecutor.datadict;

public class DDElement {
    private String elmIdf;
    private String schemaDataType;
    private String delimiter;
    private boolean hasMultipleValues = false;

    /**
     * DD element constructor
     * @param elmIdf Element id
     */
    public DDElement(String elmIdf) {
        this.elmIdf = elmIdf;
        this.schemaDataType = "xs:string";
        this.delimiter = "";
    }

    public String getSchemaDataType() {
        return schemaDataType;
    }

    public void setSchemaDataType(String dataType) {
        this.schemaDataType = dataType;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isHasMultipleValues() {
        return hasMultipleValues;
    }

    public void setHasMultipleValues(boolean hasMultipleValues) {
        this.hasMultipleValues = hasMultipleValues;
    }

    public String getElmIdf() {
        return elmIdf;
    }

    public void setElmIdf(String elmIdf) {
        this.elmIdf = elmIdf;
    }

    /**
     * DD element equals.
     * @param ddElm DD element
     * @return True if this element is equal with ddElm
     */
    public boolean equals(DDElement ddElm) {
        if (getElmIdf() != null && ddElm != null && ddElm.getElmIdf() != null) {
            return getElmIdf().equalsIgnoreCase(ddElm.getElmIdf());
        }
        return false;
    }

    @Override
    public String toString() {
        return "DDElement [elmIdf=" + elmIdf + "]";
    }

}
