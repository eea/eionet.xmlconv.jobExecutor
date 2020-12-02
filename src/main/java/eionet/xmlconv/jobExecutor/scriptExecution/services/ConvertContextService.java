package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.converters.ConvertStrategy;

import java.io.InputStream;
import java.io.OutputStream;

public interface ConvertContextService {
    String executeConversion(ConvertStrategy converter) throws Exception;
}
