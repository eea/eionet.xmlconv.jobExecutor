package eionet.xmlconv.jobExecutor.datadict;

public class DDDatasetTable {

    private String tblId;
    private String shortName;
    private String dataSet;
    private String dateReleased;

    /**
     * Constructor
     * @param tblId Table id
     */
    public DDDatasetTable(String tblId) {
        super();
        this.tblId = tblId;
    }
    /**
     * @return the tblId
     */
    public String getTblId() {
        return tblId;
    }
    /**
     * @param tblId the tblId to set
     */
    public void setTblId(String tblId) {
        this.tblId = tblId;
    }
    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }
    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    /**
     * @return the dataSet
     */
    public String getDataSet() {
        return dataSet;
    }
    /**
     * @param dataSet the dataSet to set
     */
    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }
    /**
     * @return the dateReleased
     */
    public String getDateReleased() {
        return dateReleased;
    }
    /**
     * @param dateReleased the dateReleased to set
     */
    public void setDateReleased(String dateReleased) {
        this.dateReleased = dateReleased;
    }
}
