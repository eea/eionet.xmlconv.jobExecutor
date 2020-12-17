package eionet.xmlconv.jobExecutor;

import org.junit.Ignore;

@Ignore
public class TestUtils {

    public static String getSeedURL(String seedName, Object obj) {
        //TODO
        return Properties.testHttpdUrl.concat(seedName);
    }

    public static String getLocalURL(String filename) {
        //TODO
        return Properties.testHttpdUrl.concat(filename);
    }
}
