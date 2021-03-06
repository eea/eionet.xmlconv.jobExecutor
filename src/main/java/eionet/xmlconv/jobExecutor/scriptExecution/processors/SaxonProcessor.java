package eionet.xmlconv.jobExecutor.scriptExecution.processors;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

import javax.xml.transform.stream.StreamSource;

public class SaxonProcessor {
    private static Processor processor;

    /**
     * Creating singleton with exceptions
     * {@link http://stackoverflow.com/questions/2284502/singleton-and-exception}
     */
    static {
        try {
            processor = new Processor(new StreamSource(SaxonProcessor.class.getResourceAsStream("/saxon-config.xml")));
        } catch (SaxonApiException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    /**
     * Default constructor
     */
    private SaxonProcessor() {
    }

    public static Processor getProcessor() {
        return processor;
    }

}