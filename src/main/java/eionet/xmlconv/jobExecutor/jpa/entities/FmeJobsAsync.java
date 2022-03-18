package eionet.xmlconv.jobExecutor.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "FME_JOBS_ASYNC")
public class FmeJobsAsync implements Serializable {

    @Id
    @Column(name = "JOB_ID")
    private Integer id;

    @Column(name = "FME_JOB_ID")
    private Integer fmeJobId;

    @Column(name = "RETRIES")
    private Integer retries;

    @Column(name = "COUNT")
    private Integer count;

    @Column(name = "SCRIPT", columnDefinition = "text")
    private String script;

    @Column(name = "FOLDER_NAME")
    private String folderName;

    @Column(name = "TIMESTAMP")
    private Instant timestamp;

    @Column(name = "PROCESSING")
    private boolean processing;

    public FmeJobsAsync() {
    }

    public FmeJobsAsync(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public FmeJobsAsync setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getFmeJobId() {
        return fmeJobId;
    }

    public FmeJobsAsync setFmeJobId(Integer fmeJobId) {
        this.fmeJobId = fmeJobId;
        return this;
    }

    public Integer getRetries() {
        return retries;
    }

    public FmeJobsAsync setRetries(Integer retries) {
        this.retries = retries;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public FmeJobsAsync setCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getScript() {
        return script;
    }

    public FmeJobsAsync setScript(String script) {
        this.script = script;
        return this;
    }

    public String getFolderName() {
        return folderName;
    }

    public FmeJobsAsync setFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public FmeJobsAsync setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public boolean isProcessing() {
        return processing;
    }

    public FmeJobsAsync setProcessing(boolean processing) {
        this.processing = processing;
        return this;
    }
}
