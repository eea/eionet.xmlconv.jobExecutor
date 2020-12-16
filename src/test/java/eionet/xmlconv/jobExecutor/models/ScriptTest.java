package eionet.xmlconv.jobExecutor.models;

import eionet.xmlconv.jobExecutor.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class ScriptTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testConstructor1() {
        new Script("1 + 3", new String[]{});
    }

    @Test
    public void testConstructor2() {
        new Script("1 + 3", new String[]{}, "html");
    }

}

