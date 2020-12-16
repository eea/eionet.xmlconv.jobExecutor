package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import eionet.xmlconv.jobExecutor.Properties;

public final class Utils {

    /**
     * Private constructor
     */
    private Utils() {
        // do nothing
    }
    private static Map<Character, String> xmlEscapes = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);


    /**
     * Is null or not a String
     * @param o Object
     * @return True if Null or not a string
     */
    public static boolean isNullStr(Object o) {
        if (o == null || !(o instanceof String)) {
            return true;
        }

        return isNullStr((String) o);
    }

    /**
     * Is Null or empty String
     * @param s String
     * @return True if Null or empty String
     */
    public static boolean isNullStr(String s) {
        if (s == null || s.trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Saves String to file
     * @param str String
     * @param extension Extension
     * @return Result
     * @throws IOException If an error occurs.
     */
    public static String saveStrToFile(String str, String extension) throws IOException {
        return saveStrToFile(null, str, extension);
    }


    /**
     * Stores a String in a text file.
     *
     * @param fileName - file name to save to. Can be null.
     * @param str - text to be stored
     * @param extension - file extension
     * @throws IOException If an error occurs.
     */
    public static String saveStrToFile(String fileName, String str, String extension) throws IOException {
        if (fileName == null) {
            fileName = Properties.tmpFolder + File.separatorChar + "gdem_" + System.currentTimeMillis() + "." + extension;
        } else {
            if (extension != null) {
                fileName = fileName + "." + extension;
            }
        }
        FileUtils.writeStringToFile(new File(fileName), str, "UTF-8");

        return fileName;
    }

    /**
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String getFormat(Date date, String pattern) {

        if (date == null) {
            return null;
        }

        SimpleDateFormat formatter = null;
        if (pattern == null) {
            formatter = new SimpleDateFormat();
        } else {
            formatter = new SimpleDateFormat(pattern);
        }

        return formatter.format(date);
    }

    /**
     * formats timestamp (millis from 1 Jan 1970) into string using pattern.
     *
     * @param timestamp
     * @return Date object
     */
    public static String formatTimestampDate(String timestamp) {

        if (timestamp == null) {
            return null;
        }

        long l = 0;
        try {
            l = Long.parseLong(timestamp);
        } catch (Exception e) {
            return null;
        }

        Date d = new Date(l);
        return Utils.getDate(d);
    }

    /**
     * parses String to Date.
     *
     * @param srtDate
     * @param pattern
     * @return Date object
     * @throws ParseException
     */
    public static Date parseDate(String srtDate, String pattern) throws ParseException {

        if (isNullStr(srtDate)) {
            return null;
        }

        SimpleDateFormat formatter = null;
        if (pattern == null) {
            formatter = new SimpleDateFormat();
        } else {
            formatter = new SimpleDateFormat(pattern);
        }

        return formatter.parse(srtDate);
    }


    /**
     *
     * @param date
     * @return
     */
    public static String getDate(Date date) {
        return getFormat(date, Properties.dateFormatPattern);
    }

    /**
     * The method escape all suspicious characters in string for using it in XML.
     *
     * @param text
     *            any string
     * @return XML escaped string
     */
    public static String escapeXML(String text) {

        if (text == null) {
            return "";
        }
        if (text.length() == 0) {
            return text;
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            buf.append(escapeXML(i, text));
        }

        return buf.toString();
    }

    /**
     * Escape single character in text. If the character is already escaped, then avoid double escaping.
     *
     * @param pos
     *            Character position in text.
     * @param text
     *            Text to be escaped.
     * @return Return escaped character.
     */
    public static String escapeXML(int pos, String text) {

        if (xmlEscapes == null) {
            setXmlEscapes();
        }
        Character c = new Character(text.charAt(pos));

        for (String esc : xmlEscapes.values()) {
            if (pos + esc.length() < text.length()) {
                String sub = text.substring(pos, pos + esc.length());
                if (sub.equals(esc)) {
                    return c.toString();
                }
            }
        }

        if (pos + 1 < text.length() && text.charAt(pos + 1) == '#') {
            int semicolonPos = text.indexOf(';', pos + 1);
            if (semicolonPos != -1) {
                String sub = text.substring(pos + 2, semicolonPos);
                if (sub != null) {
                    try {
                        // if the string between # and ; is a number then return
                        // true,
                        // because it is most probably an escape sequence
                        if (Integer.parseInt(sub) >= 0) {
                            return c.toString();
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }

        String esc = xmlEscapes.get(c);
        if (esc != null) {
            return esc;
        } else if ((int) c > 10000) {
            return "&#" + (int) c + ";";
        } else {
            return c.toString();
        }
    }

    /**
     * Set XML character escapes.
     */
    private static void setXmlEscapes() {
        xmlEscapes = new HashMap<Character, String>();
        xmlEscapes.put(new Character('&'), "&amp;");
        xmlEscapes.put(new Character('<'), "&lt;");
        xmlEscapes.put(new Character('>'), "&gt;");
        xmlEscapes.put(new Character('"'), "&quot;");
        xmlEscapes.put(new Character('\''), "&apos;");
        xmlEscapes.put('\u001A', "?");
    }


    /**
     * A method for encoding the BASIC auth for request header.
     */
    public static String getEncodedAuthentication(String user, String pwd) throws java.io.IOException {
        String auth = user + ":" + pwd;
        String ret = new String(Base64.encodeBase64(auth.getBytes()));
        return ret;
    }

    /**
     * generates unique temporary file name with full path.
     *
     * @param fileName
     * @return
     */
    public static String getUniqueTmpFileName(String fileName) {
        StringBuilder buf = new StringBuilder();
        buf.append(Properties.tmpFolder + File.separator);
        buf.append(Constants.TMP_FILE_PREFIX);
        buf.append(System.currentTimeMillis());
        buf.append("-" + UUID.randomUUID());
        if (fileName != null) {
            if (!fileName.startsWith(".")) {
                buf.append("-");
            }
            buf.append(fileName);
        } else {
            buf.append(".tmp");
        }

        return buf.toString();
    }

    /**
     * Method constructs a URI from specified file and folder path. If the file or folder does not exists, then it return null
     * value.
     *
     * @param strPath
     *            Folder path. eg: /usr/prj/xmlconv/xmlfiles
     * @param isDirectory
     *            return URI only, if the path is directory
     * @return URI: file:///usr/prj/xmlconv/xmlfiles
     */
    public static String getURIfromPath(String strPath, boolean isDirectory) {

        if (strPath != null) {
            File f = new File(strPath);
            if (f.exists() && ((isDirectory && f.isDirectory()) || !isDirectory)) {
                return f.toURI().toString();
            }
        }
        return null;
    }

    /**
     *
     * @param in
     * @param out
     * @throws Exception
     */
    public static void copyFile(File in, File out) throws Exception {

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(in);
            fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * Returns the contents of the file in a byte array.
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = null;
        byte[] bytes = null;

        try {
            is = new FileInputStream(file);
            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

        } finally {
            // Close the input stream and return bytes
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return bytes;
    }

    /**
     * A method for replacing substrings in string.
     * @param str String
     * @param oldStr Old string
     * @param replace String to replace
     * @return New string
     */
    public static String Replace(String str, String oldStr, String replace) {
        str = (str != null ? str : "");

        StringBuffer buf = new StringBuffer();
        int found = 0;
        int last = 0;

        while ((found = str.indexOf(oldStr, last)) >= 0) {
            buf.append(str.substring(last, found));
            buf.append(replace);
            last = found + oldStr.length();
        }
        buf.append(str.substring(last));
        return buf.toString();
    }

    /*
     *
     */
    public static void deleteFolder(String folderPath) {

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteFolder(files[i].getAbsolutePath());
                continue;
            }
            files[i].delete();
        }

        folder.delete();
    }

    /**
            * Deletes file
     * @param fileName file name
     */
    public static void deleteFile(String fileName) {
        deleteFile(new File(fileName));
    }

    /**
     * Deletes file
     * @param file file
     */
    public static void deleteFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            try {
                file.delete();
            } catch (SecurityException e) {
                LOGGER.error("Security exception when trying to delete " + file, e);
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected RuntimeException when trying to delete " + file, e);
            }
        }
    }

    /**
     * Creates random name using timestamp.
     */
    public static String getRandomName() {

        StringBuffer bufRandName = new StringBuffer(32);
        bufRandName.append(System.currentTimeMillis());
        bufRandName.append(Math.random() * 10000);
        return bufRandName.toString();
    }


    /**
     * Is Null or empty list
     * @param l List
     * @return True if Null or empty list
     */
    public static boolean isNullList(List l) {
        if (l == null) {
            return true;
        } else if (l.size() == 0) {
            return true;
        }

        return false;
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getDateTime(Date date) {
        return getFormat(date, Properties.timeFormatPattern);
    }

    /**
     * Checks if the given string is a well-formed URL.
     * @param s String
     * @return True if is a well-formed URL
     */
    public static boolean isURL(String s) {
        try {
            new URL(s);
        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }


    /**
     * Is Null or empty hash map
     * @param h hash map
     * @return True if Null or empty hash map
     */
    public static boolean isNullHashMap(Map h) {
        if (h == null) {
            return true;
        } else if (h.isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean containsKeyIgnoreCase(Map<String, String> hash, String val) {

        Iterator<String> keysIterator = hash.keySet().iterator();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            if (key.equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the XML declaration from instance file.
     */
    public static String getEncodingFromStream(String str_url) {
        BufferedReader br = null;
        try {
            URL url = new URL(str_url);
            // ins = new DataInputStream(url.openStream());
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String xml_decl = br.readLine();

            if (xml_decl == null) {
                return null;
            }
            if (!xml_decl.startsWith("<?xml version=") && !xml_decl.endsWith("?>")) {
                return null;
            }
            int idx = xml_decl.indexOf("encoding=");
            if (idx == -1) {
                return null;
            }
            String start = xml_decl.substring(idx + 10);
            int end_idx = start.indexOf("\"");
            if (end_idx == -1) {
                return null;
            }
            String enc = start.substring(0, end_idx);

            return enc;
        } catch (MalformedURLException e) {
            LOGGER.debug("It is not url: " + str_url + "; " + e.toString());
            return null;
        } catch (IOException e) {
            LOGGER.debug("could not read encoding from url: " + str_url + "; " + e.toString());
            return null;
        } catch (Exception e) {
            return null;
            // couldn't read encoding
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LOGGER.error("Exception: ", e);
            }
        }
    }

    /**
     * Checks if list contains any String values or not.
     *
     * @param list
     *            The list that will be investigated
     *
     * @return value true, if the list does not contain any String values, otherwise true
     */
    public static boolean isEmptyList(List<String> list) {
        boolean ret = true;
        if (list == null) {
            return ret;
        }
        if (list.size() == 0) {
            return ret;
        }

        for (int i = 0; i < list.size(); i++) {
            String str_value = list.get(i);
            if (!Utils.isNullStr(str_value)) {
                return false;
            }
        }

        return ret;
    }

}
