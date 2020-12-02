package eionet.xmlconv.jobExecutor.objects;

public class RowColumnDefinition {
    private String dataType;
    private short styleIndex;
    private String styleName;

    /**
     * Constructor.
     * @param dataType Data type
     * @param styleIndex Style index
     * @param styleName Style name
     */
    public RowColumnDefinition(String dataType, short styleIndex, String styleName) {
        this.dataType = dataType;
        this.styleIndex = styleIndex;
        this.styleName = styleName;
    }

    public String getDataType() {
        return dataType;
    }

    public short getStyleIndex() {
        return styleIndex;
    }

    public String getStyleName() {
        return styleName;
    }
}
