package eionet.xmlconv.jobExecutor.models;

import java.io.Serializable;

/**
 * CDR file data transfer object.
 */
public class CdrFileDto implements Serializable {

    private String url;
    private String country;
    private String partofyear;
    private int endyear;
    private int year;
    private String title;
    private String iso;

    /**
     * Default constructor
     */
    public CdrFileDto() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPartofyear() {
        return partofyear;
    }

    public void setPartofyear(String partofyear) {
        this.partofyear = partofyear;
    }

    public int getEndyear() {
        return endyear;
    }

    public void setEndyear(int endyear) {
        this.endyear = endyear;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    /**
     * Gets file label
     * @return Label
     */
    public String getLabel() {
        StringBuilder label = new StringBuilder(country);
        label.append(" - ");
        label.append(title);
        if (getYear() > 0) {
            label.append(" - (");
            label.append(year);
            if (getEndyear() > 0) {
                label.append(" - ");
                label.append(getEndyear());
            }
            if (getEndyear() == 0) {
                label.append(" - ");
                label.append(getPartofyear());
            }
            label.append(")");
        }
        return label.toString();
    }
}
