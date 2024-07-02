package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.LogDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogDeviceServiceImpl implements LogDeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogDeviceService.class);

    @Autowired
    public LogDeviceServiceImpl() {
    }

    @Override
    public void log(String c) {
        LOGGER.debug(c);
    }

}
