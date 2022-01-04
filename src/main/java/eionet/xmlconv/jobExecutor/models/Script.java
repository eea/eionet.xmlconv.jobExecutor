package eionet.xmlconv.jobExecutor.models;


import eionet.xmlconv.jobExecutor.Constants;
import eionet.xmlconv.jobExecutor.scriptExecution.services.ScriptEngineService;

/*This class was previously named XQScript*/

/*This class is used as a parameter in the getResult method*/
public class Script {

    private String[] params; // parameter name + value pairs
    private String strResultFile;
    private String scriptSource; // XQuery script
    private String outputType; // html, txt, xml, zip
    private String scriptType; // xquery, xsl
    private String scriptFileName; // full path of script file
    private String srcFileUrl;
    private Schema schema;
    private String jobId;
    private Boolean asynchronousExecution;
    private String fmeJobId;

    private boolean srcFileDownloaded;

    public static final String SCRIPT_LANG_XQUERY1 = "xquery 1.0";
    public static final String SCRIPT_LANG_XQUERY3 = "xquery 3.0+";
    public static final String SCRIPT_LANG_XSL = "xsl";
    public static final String SCRIPT_LANG_FME = "fme";

    public static final String[] SCRIPT_LANGS = {SCRIPT_LANG_XQUERY3, SCRIPT_LANG_XQUERY1, SCRIPT_LANG_XSL, SCRIPT_LANG_FME };


    public static final String SCRIPT_RESULTTYPE_XML = "XML";
    public static final String SCRIPT_RESULTTYPE_TEXT = "TEXT";
    public static final String SCRIPT_RESULTTYPE_HTML = "HTML";
    public static final String SCRIPT_RESULTTYPE_ZIP = "ZIP";

    public static final String[] SCRIPT_RESULTTYPES = {SCRIPT_RESULTTYPE_HTML, SCRIPT_RESULTTYPE_XML, SCRIPT_RESULTTYPE_TEXT, SCRIPT_RESULTTYPE_ZIP};


    // XQ Engine instance
    private ScriptEngineService engine;

    public Script() {
    }

    /**
     * @param script Script
     * @param scriptParams
     *            XQ parameter name + value pairs in an array in format {name1=value1, name2=value2, ... , nameN=valueN} if no
     *            parameters, null should be passed
     */
    public Script(String script, String[] scriptParams) {
        this(script, scriptParams, ScriptEngineService.DEFAULT_OUTPUTTYPE);
    }

    /**
     * Constructor
     * @param script Script
     * @param scriptParams Parameters
     * @param outputType Output type
     */
    public Script(String script, String[] scriptParams, String outputType) {
        this.scriptSource = script;
        this.params = scriptParams;
        this.outputType = outputType;
        scriptType = SCRIPT_LANG_XQUERY1;
    }


    /**
     * Returns original file URL.
     * @return File URL
     */
    public String getOrigFileUrl() {
        if (srcFileUrl != null && srcFileUrl.indexOf(Constants.GETSOURCE_URL) > -1
                && srcFileUrl.indexOf(Constants.SOURCE_URL_PARAM) > -1) {

            return (srcFileUrl.substring(srcFileUrl.indexOf(Constants.SOURCE_URL_PARAM) + Constants.SOURCE_URL_PARAM.length() + 1));
        }

        return srcFileUrl;
    }


    public void setResultFile(String fileName) {
        strResultFile = fileName;
    }

    public String getStrResultFile() {
        return strResultFile;
    }

    public void setStrResultFile(String strResultFile) {
        this.strResultFile = strResultFile;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getSrcFileUrl() {
        return srcFileUrl;
    }

    public void setSrcFileUrl(String srcFileUrl) {
        this.srcFileUrl = srcFileUrl;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String getScriptSource() {
        return scriptSource;
    }

    public void setScriptSource(String scriptSource) {
        this.scriptSource = scriptSource;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    public boolean isSrcFileDownloaded() {
        return srcFileDownloaded;
    }

    public void setSrcFileDownloaded(boolean srcFileDownloaded) {
        this.srcFileDownloaded = srcFileDownloaded;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ScriptEngineService getEngine() {
        return engine;
    }

    public void setEngine(ScriptEngineService engine) {
        this.engine = engine;
    }

    public Boolean getAsynchronousExecution() {
        return asynchronousExecution;
    }

    public void setAsynchronousExecution(Boolean asynchronousExecution) {
        this.asynchronousExecution = asynchronousExecution;
    }

    public String getFmeJobId() {
        return fmeJobId;
    }

    public void setFmeJobId(String fmeJobId) {
        this.fmeJobId = fmeJobId;
    }
}
