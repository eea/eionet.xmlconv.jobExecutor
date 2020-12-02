package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.scriptExecution.services.SourceReaderService;
import eionet.xmlconv.jobExecutor.scriptExecution.services.impl.readers.OdsReaderServiceImpl;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OpenDocumentUtils {

    /**
     * returns a valid SpreadsheetReaderIF
     */
    public static SourceReaderService getSpreadhseetReader() {
        return new OdsReaderServiceImpl();
    }
    /**
     * Returns true, if inputstream is zip file
     * @param input InputStream
     * @return True if InputStream is a zip file.
     */
    public static boolean isSpreadsheetFile(InputStream input) {

        ZipInputStream zipStream = null;
        ZipEntry zipEntry = null;
        try {
            zipStream = new ZipInputStream(input);
            while (zipStream.available() == 1 && (zipEntry = zipStream.getNextEntry()) != null) {
                if (zipEntry != null) {
                    if ("content.xml".equals(zipEntry.getName())) {
                        // content file found, it is OpenDocument.
                        return true;
                    }
                }
            }
        } catch (IOException ioe) {
            return false;
        } finally {
            IOUtils.closeQuietly(zipStream);
        }
        return false;

    }
}
