/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
