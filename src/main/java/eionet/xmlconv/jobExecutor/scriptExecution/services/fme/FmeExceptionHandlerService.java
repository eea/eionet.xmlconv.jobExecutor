package eionet.xmlconv.jobExecutor.scriptExecution.services.fme;

import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.models.Script;
import java.io.IOException;

public interface FmeExceptionHandlerService {

    void execute(Script script, String fmeJobId, String exceptionMessage) throws DatabaseException, IOException;
}
