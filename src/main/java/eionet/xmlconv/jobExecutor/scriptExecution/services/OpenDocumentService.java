package eionet.xmlconv.jobExecutor.scriptExecution.services;

import java.io.OutputStream;

public interface OpenDocumentService {
    void setContentFile(String strContentFile);
    public void createOdsFile(String strOut) throws Exception;
    public void createOdsFile(OutputStream out) throws Exception;
}
