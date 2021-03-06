package eionet.xmlconv.jobExecutor.models;

import eionet.xmlconv.jobExecutor.exceptions.ScriptExecutionException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CustomURI {

    private URI uri;

    public CustomURI(String url) throws URISyntaxException {
        try {
            uri = new URI(url.replace(" ", "%20"));
        } catch (URISyntaxException e) {
            // in case of windows paths...
            uri = new File(url).toPath().toUri();
        }
    }

    public String getHost() {
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    public URI getUri() {
        return uri;
    }

    public String getHttpURL() throws ScriptExecutionException {
        try {
            return uri.toURL().toString();
        } catch (MalformedURLException e) {
            throw new ScriptExecutionException("Error in URL", e);
        }
    }

    public URL getRawURL() throws MalformedURLException {
        URL temp = null;
        try {
            temp = uri.toURL();
        } catch (IllegalArgumentException ae) {
            throw new MalformedURLException(ae.toString());
        }
        return temp;
    }

}
