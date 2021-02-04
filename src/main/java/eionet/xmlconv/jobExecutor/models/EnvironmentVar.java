package eionet.xmlconv.jobExecutor.models;

public class EnvironmentVar {
    public String fName = null;
    public String fValue = null;

    /**
     * Constructor
     * @param name Name
     * @param value Value
     */
    public EnvironmentVar(String name, String value) {
        fName = name;
        fValue = value;
    }
}
