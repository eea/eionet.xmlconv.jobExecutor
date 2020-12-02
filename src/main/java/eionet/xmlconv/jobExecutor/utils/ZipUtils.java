package eionet.xmlconv.jobExecutor.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {

    static final int BUFFER = 2156;
    static final String MIMETYPE_FILE = "mimetype";

    /**
     * Private constructor.
     */
    private ZipUtils() {
        // do nothing
    }

    /**
     * Zips Directory to zip file
     * @param dir2zip Directory
     * @param outZip Output zip file
     * @throws IOException
     */
    public static void zipDir(String dir2zip, ZipOutputStream outZip) throws IOException {
        zipDir(dir2zip, outZip, dir2zip);
    }

    /**
     * Function zips all the files in directory and subdirectories and adds these into zip file
     *
     * @param dir2zip
     *            - directory that will be zipped
     * @param outZip
     *            - ZipOutputStream represents the zip file, where the files will be placed
     * @param sourceDir
     *            - root directory, where the zipping started
     * @throws IOException If an error occurs.
     */
    public static void zipDir(String dir2zip, ZipOutputStream outZip, String sourceDir) throws IOException {
        // create a new File object based on the directory we have to zip
        File zipDir = new File(dir2zip);
        // get a listing of the directory content
        String[] dirList = zipDir.list();

        // if directory contains mimetype file, then start with it
        File mimetype_file = new File(dir2zip, MIMETYPE_FILE);
        if (mimetype_file.exists()) {
            // Set the compression ratio
            zipFile(mimetype_file, outZip, sourceDir, false);
        }

        // loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {

            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                // if the File object is a directory, call this
                // function again to add its content recursively
                String filePath = f.getPath();
                zipDir(filePath, outZip, sourceDir);
                // loop again
                continue;
            }
            // Do not zip mimetype file anymore
            if (dirList[i].equals(MIMETYPE_FILE)) {
                continue;
            }

            // if we reached herem the f is not directory
            zipFile(f, outZip, sourceDir, true);
        }
    }

    /**
     *
     * @param f
     *            - File that will be zipped
     * @param outZip
     *            - ZipOutputStream represents the zip file, where the files will be placed
     * @param sourceDir
     *            - root directory, where the zipping started
     * @param doCompress
     *            - don't comress the file, if doCompress=true
     * @throws IOException If an error occurs.
     */
    public static void zipFile(File f, ZipOutputStream outZip, String sourceDir, boolean doCompress) throws IOException {

        // Read the source file into byte array
        byte[] fileBytes = Utils.getBytesFromFile(f);

        // create a new zip entry
        String strAbsPath = f.getPath();
        String strZipEntryName = strAbsPath.substring(sourceDir.length() + 1, strAbsPath.length());
        strZipEntryName = Utils.Replace(strZipEntryName, File.separator, "/");
        ZipEntry anEntry = new ZipEntry(strZipEntryName);

        // Don't compress the file, if not needed
        if (!doCompress) {
            // ZipEntry can't calculate crc size automatically, if we use STORED method.
            anEntry.setMethod(ZipEntry.STORED);
            anEntry.setSize(fileBytes.length);
            CRC32 crc321 = new CRC32();
            crc321.update(fileBytes);
            anEntry.setCrc(crc321.getValue());
        }
        // place the zip entry in the ZipOutputStream object
        outZip.putNextEntry(anEntry);

        // now write the content of the file to the ZipOutputStream
        outZip.write(fileBytes);

        outZip.flush();
        // Close the current entry
        outZip.closeEntry();

    }

    /**
     * Unzips files to output directory.
     * @param inZip Zip file
     * @param outDir Output directory
     * @throws IOException If an error occurs.
     */
    public static void unzip(String inZip, String outDir) throws IOException {

        File sourceZipFile = new File(inZip);
        File unzipDestinationDirectory = new File(outDir);

        // Open Zip file for reading
        ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

        // Create an enumeration of the entries in the zip file
        Enumeration zipFileEntries = zipFile.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();
            // System.out.println("Extracting: " + entry);

            File destFile = new File(unzipDestinationDirectory, currentEntry);

            // grab file's parent directory structure
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            // extract file if not a directory
            if (!entry.isDirectory()) {
                BufferedInputStream is = null;
                BufferedOutputStream dest = null;
                try{
                    is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte[] data = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                }
                finally{
                    IOUtils.closeQuietly(dest);
                    IOUtils.closeQuietly(is);
                }
            }
        }
        zipFile.close();
    }

    /**
     * Find file type in directory
     * @param dir Directory
     * @return True if file type exists in directory.
     */
    private static boolean getDirContainsMimeFile(String dir) {
        File file = new File(dir, MIMETYPE_FILE);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
