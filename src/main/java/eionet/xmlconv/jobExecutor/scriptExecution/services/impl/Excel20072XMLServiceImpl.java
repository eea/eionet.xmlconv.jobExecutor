package eionet.xmlconv.jobExecutor.scriptExecution.services.impl;

import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Excel20072XMLServiceImpl extends DDXMLConverterServiceImpl{
    /**
     * Class constructor.
     */
    @Autowired
    Excel20072XMLServiceImpl(){
        super();
    }

    private static final String FORMAT_NAME = "MS Excel 2007";

    @Override
    public SourceReaderService getSourceReader() {
        return ExcelUtils.getExcel2007Reader();
    }

    @Override
    public String getSourceFormatName() {
        return FORMAT_NAME;
    }
}
