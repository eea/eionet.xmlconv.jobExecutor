package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.converters.ConvertStrategy;

public interface ConvertContextService {
    String executeConversion(ConvertStrategy converter) throws Exception;
}
