package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.utils.OpenDocumentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ods2XmlServiceImpl extends DDXMLConverterServiceImpl{
    /**
     * Class constructor.
     */
    @Autowired
    public Ods2XmlServiceImpl(){
        super();
    }

    private static final String FORMAT_NAME = "OpenDocument Spreadsheet";

    @Override
    public SourceReaderService getSourceReader() {
        return OpenDocumentUtils.getSpreadhseetReader();
    }

    @Override
    public String getSourceFormatName() {
        return FORMAT_NAME;
    }
}
