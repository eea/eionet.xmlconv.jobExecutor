package eionet.xmlconv.jobExecutor.scriptExecution.services;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.models.ConversionResultDto;

import java.io.File;
import java.io.OutputStream;

public interface DDXMLConverterService {
    void initConverter(File inFile) throws ConversionException;
    void startConverter(ConversionResultDto resultObject, String sheetParam) throws ConversionException;
    ConversionResultDto convertDD_XML(OutputStream outStream) throws ConversionException;
    ConversionResultDto convertDD_XML_split(OutputStream outStream, String sheetParam) throws ConversionException;
    DDXMLConverterService getConverter(File inFile, ConversionResultDto resultObject, String sheetParam) throws ConversionException;
    String getInvalidSchemaMessage(String xmlSchema) throws ConversionException;


}
