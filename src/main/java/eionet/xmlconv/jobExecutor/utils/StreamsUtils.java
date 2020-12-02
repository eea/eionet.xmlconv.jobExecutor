package eionet.xmlconv.jobExecutor.utils;

import java.io.*;

public final class StreamsUtils {

    /**
     * Private constructor
     */
    private StreamsUtils() {
        // do nothing
    }
    static final int BLOCK_SIZE = 4096;

    /**
     * Drains InputStream
     * @param r Read
     * @param w Write
     * @throws IOException If an error occurs.
     */
    public static void drain(InputStream r, OutputStream w) throws IOException {
        byte[] bytes = new byte[BLOCK_SIZE];
        try {
            int length = r.read(bytes);
            while (length != -1) {
                if (length != 0) {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally {
            bytes = null;
        }

    }

    /**
     * Drains InputStream
     * @param r Read
     * @param w Write
     * @throws IOException If an error occurs.
     */
    public static void drain(Reader r, Writer w) throws IOException {
        char[] bytes = new char[BLOCK_SIZE];
        try {
            int length = r.read(bytes);
            while (length != -1) {
                if (length != 0) {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally {
            bytes = null;
        }

    }

    /**
     * Drains InputStream
     * @param r Read
     * @param os OutputStream
     * @throws IOException If an error occurs.
     */
    public static void drain(Reader r, OutputStream os) throws IOException {
        Writer w = new OutputStreamWriter(os);
        drain(r, w);
        w.flush();
    }

    /**
     * Drains InputStream
     * @param is InputStream
     * @param w Writer
     * @throws IOException If an error occurs.
     */
    public static void drain(InputStream is, Writer w) throws IOException {
        Reader r = new InputStreamReader(is);
        drain(r, w);
        w.flush();
    }

    /**
     * Drains Stream
     * @param r InputStream
     * @return Content
     * @throws IOException If an error occurs.
     */
    public static byte[] drain(InputStream r) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        drain(r, bytes);
        return bytes.toByteArray();
    }

    /**
     * Returns Stream as String
     * @param is InputStream
     * @return Content
     */
    public static String getAsString(InputStream is) {
        int c = 0;
        char[] lineBuffer = new char[128];
        char[] buf = lineBuffer;
        int room = buf.length, offset = 0;
        try {
            loop: while (true) {
                // read chars into a buffer which grows as needed
                switch (c = is.read()) {
                    case -1:
                        break loop;

                    default:
                        if (--room < 0) {
                            buf = new char[offset + 128];
                            room = buf.length - offset - 1;
                            System.arraycopy(lineBuffer, 0, buf, 0, offset);
                            lineBuffer = buf;
                        }
                        buf[offset++] = (char) c;
                        break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

}
