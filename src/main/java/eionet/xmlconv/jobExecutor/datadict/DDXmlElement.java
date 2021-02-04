package eionet.xmlconv.jobExecutor.datadict;

public class DDXmlElement {
    private String name;
    private String localName;
    private String attributes;
    private int colIndex = -1;
    private boolean isMainTable = false;

    /**
     * DD Xml Element constructor
     * @param name Element name
     * @param localName Element local name
     * @param attributes Element attributes
     */
    public DDXmlElement(String name, String localName, String attributes) {
        super();
        this.name = name;
        this.localName = localName;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }

    public String getAttributes() {
        return attributes;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public boolean isMainTable() {
        return isMainTable;
    }

    public void setMainTable(boolean isMainTable) {
        this.isMainTable = isMainTable;
    }
}
