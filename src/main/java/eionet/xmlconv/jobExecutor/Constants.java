package eionet.xmlconv.jobExecutor;

public final class Constants {

    /**
     * Private constructor to deal with reflection.
     */
    private Constants() {
        throw new AssertionError();
    }
    public static final String WARNING_QA_EXPIRED_SCHEMA = "The reported XML file uses an expired XML Schema. Schema expire date is {0}.";
    public static final String WARNING_QA_EXPIRED_DD_SCHEMA = "The reported XML file uses an obsolete version of Data Dictionary XML Schema. The last version of given dataset is released on {0} with ID={1}.";
    public static final String GETSOURCE_URL = "/s/getsource";
    public static final String SOURCE_URL_PARAM = "source_url";
}
