package eionet.xmlconv.jobExecutor.objects;

import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import eionet.xmlconv.jobExecutor.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The object stores all the needed information about the converted file.
 **/
public class ConvertedFileDto {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertedFileDto.class);

    private String fileName;
    private String filePath;
    private String fileUrl;

    /**
     * Class constructor.
     *
     * @param fileName File name
     * @param filePath File path
     */
    public ConvertedFileDto(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Get file content as byte array
     * @return File contents
     * @throws ConversionException If an error occurs.
     */
    public byte[] getFileContentAsByteArray() throws ConversionException {
        FileInputStream fis = null;
        File convFile = new File(getFilePath());
        byte[] result;
        try {
            fis = new FileInputStream(convFile);
            result = IOUtils.toByteArray(fis);
        } catch (IOException e) {
            LOGGER.error("Converted file not found: " + getFilePath());
            throw new ConversionException("Converted file not found: " + getFileName());
        } finally {
            IOUtils.closeQuietly(fis);
        }
        Utils.deleteFile(convFile);
        return result;
    }
}
