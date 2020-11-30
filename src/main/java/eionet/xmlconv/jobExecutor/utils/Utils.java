package eionet.xmlconv.jobExecutor.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

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

}
