package eionet.xmlconv.jobExecutor.objects;


import java.io.Serializable;

public class CrFileDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String url;

    private String lastModified;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets label
     * @return Label
     */
    public String getLabel() {
        StringBuilder label = new StringBuilder(getUrl());
        if (getLastModified() != null && getLastModified().length() > 0) {
            label.append(" - (modified: ");
            label.append(getLastModified());
            label.append(")");
        }

        return label.toString();
    }
}
