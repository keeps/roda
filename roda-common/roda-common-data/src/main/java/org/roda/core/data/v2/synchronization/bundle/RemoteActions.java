package org.roda.core.data.v2.synchronization.bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RemoteActions {
    private List<String> jobList= new ArrayList<>();

    public List<String> getJobList() {
        return jobList;
    }

    public void setJobList(List<String> jobList) {
        this.jobList = jobList;
    }

    public void addToJobList(String jobId) {
        this.jobList.add(jobId);
    }
}
