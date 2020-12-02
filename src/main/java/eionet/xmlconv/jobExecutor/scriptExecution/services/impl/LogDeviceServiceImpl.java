package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.LogDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDeviceServiceImpl implements LogDeviceService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogDeviceService.class);
    @Override
    public void log(String c) {
        LOGGER.debug(c);
    }

}
