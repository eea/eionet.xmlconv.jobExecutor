package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.LogDeviceServiceImpl;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.SysCommandExecutorServiceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class, Constants.class })
@RunWith(SpringRunner.class)
public class SysCommandExecutorServiceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * The method tests, if the system is able to execute some simple commands
     *
     * @throws Exception
     */
    @Test
    public void testCommand() throws Exception {
        SysCommandExecutorService exe = new SysCommandExecutorServiceImpl();
        exe.setOutputLogDevice(new LogDeviceServiceImpl());
        exe.setErrorLogDevice(new LogDeviceServiceImpl());
        exe.setTimeout(20000L);
        int status = exe.runCommand("echo OK");
        Thread.sleep(1000);
        String out = exe.getCommandOutput();

        assertEquals(0, status);
        assertEquals("OK" + System.getProperty("line.separator"), out);
    }

    /**
     * The method tests, if it's possible to kill the process after timeout
     *
     * @throws Exception
     */
    @Test
    public void testCommandTimeout() throws Exception {
        SysCommandExecutorService exe = new SysCommandExecutorServiceImpl();
        exe.setOutputLogDevice(new LogDeviceServiceImpl());
        exe.setErrorLogDevice(new LogDeviceServiceImpl());
        exe.setTimeout(1L); // 1 second
        exception.expect(RuntimeException.class);
        exception.reportMissingExceptionWithMessage("No expected exception: %s");
        int status = exe.runCommand("sleep 3");
    }
}
