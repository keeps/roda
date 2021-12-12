package org.roda.core.data.v2.synchronization.bundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AttachmentState implements Serializable {
    private static final long serialVersionUID = -2011949703455939727L;

    private String jobId;
    private List<String> attachmentIdList = new ArrayList<>();
    private String checksum;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @JsonProperty(value = "attachments")
    public List<String> getAttachmentIdList() {
        return attachmentIdList;
    }

    @JsonIgnore
    public void setAttachmentIdList(List<String> attachmentIdList) {
        this.attachmentIdList = attachmentIdList;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
