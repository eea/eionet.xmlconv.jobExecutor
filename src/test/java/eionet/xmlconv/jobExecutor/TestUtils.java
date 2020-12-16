package eionet.xmlconv.jobExecutor;
import org.junit.Ignore;

@Ignore
public class TestUtils {
    /**
     * Set up test runtime properties
     *
     * @param obj
     */
    public static void setUpProperties(Object obj) {
        Properties.metaXSLFolder = Properties.appRootFolder + "/dcm";
        Properties.convFile = Properties.metaXSLFolder + "/conversions.xml";
    }
}
