package eionet.xmlconv.jobExecutor.utils;

import eionet.xmlconv.jobExecutor.scriptExecution.services.LogDeviceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AsyncStreamReader extends Thread{
    private StringBuffer fBuffer = null;
    private InputStream fInputStream = null;
    private String fThreadId = null;
    private boolean fStop = false;
    private LogDeviceService fLogDevice = null;

    private String fNewLine = null;

    /**
     * Constructor
     * @param inputStream InputStream
     * @param buffer Buffer
     * @param logDevice Log device
     * @param threadId Thread Id
     */
    public AsyncStreamReader(InputStream inputStream, StringBuffer buffer, LogDeviceService logDevice, String threadId) {
        fInputStream = inputStream;
        fBuffer = buffer;
        fThreadId = threadId;
        fLogDevice = logDevice;

        fNewLine = System.getProperty("line.separator");
    }

    public String getBuffer() {
        return fBuffer.toString();
    }

    /**
     * Runs stream reader.
     * TODO: possibility of adding logging
     */
    public void run() {
        try {
            readCommandOutput();
        } catch (Exception ex) {
            // ex.printStackTrace(); //DEBUG
        }
    }

    /**
     * Reads command output
     * @throws IOException If an error occurs.
     */
    private void readCommandOutput() throws IOException {
        BufferedReader bufOut = new BufferedReader(new InputStreamReader(fInputStream));
        String line = null;
        while ((!fStop) && ((line = bufOut.readLine()) != null)) {
            fBuffer.append(line + fNewLine);
            printToDisplayDevice(line);
        }
        bufOut.close();
        // printToConsole("END OF: " + fThreadId); //DEBUG
    }

    /**
     * Stops reading.
     */
    public void stopReading() {
        fStop = true;
    }

    /**
     * Prints to display device
     * @param line Line to print
     */
    private void printToDisplayDevice(String line) {
        if (fLogDevice != null)
            fLogDevice.log(line);
        else {
            // printToConsole(line);//DEBUG
        }
    }

    /**
     * Prints to console
     * TODO: remove maybe
     * @param line Line
     */
    private synchronized void printToConsole(String line) {
        System.out.println(line);
    }
}
