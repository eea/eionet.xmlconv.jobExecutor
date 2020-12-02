package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Excel2XMLServiceImpl extends DDXMLConverterServiceImpl{
    /**
     * Class constructor.
     */
    @Autowired
    Excel2XMLServiceImpl(){
        super();
    }

    private static final String FORMAT_NAME = "MS Excel";

    @Override
    public SourceReaderService getSourceReader() {
        return ExcelUtils.getExcelReader();
    }

    @Override
    public String getSourceFormatName() {
        return FORMAT_NAME;
    }
}
