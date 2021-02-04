package eionet.xmlconv.jobExecutor;

import org.junit.Ignore;

@Ignore
public class TestUtils {

    public static String getSeedURL(String seedName, Object obj) {
        return Properties.testHttpdUrl.concat(seedName);
    }

}
