/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobReport implements Serializable {
  private static final long serialVersionUID = -2028521062931876576L;

  public enum PluginState {
    SUCCESS, PARTIAL_SUCCESS, FAILURE
  }

  private String id = null;
  private Date dateCreated = null;
  private Date dateUpdated = null;
  private String jobId = null;
  private String aipId = null;
  private String objectId = null;
  private String lastPluginRan = null;
  private PluginState lastPluginRanState = null;
  private Report report = null;

  public JobReport() {
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
    result = prime * result + ((dateUpdated == null) ? 0 : dateUpdated.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
    result = prime * result + ((lastPluginRan == null) ? 0 : lastPluginRan.hashCode());
    result = prime * result + ((lastPluginRanState == null) ? 0 : lastPluginRanState.hashCode());
    result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
    result = prime * result + ((report == null) ? 0 : report.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JobReport)) {
      return false;
    }
    JobReport other = (JobReport) obj;
    if (aipId == null) {
      if (other.aipId != null) {
        return false;
      }
    } else if (!aipId.equals(other.aipId)) {
      return false;
    }
    if (dateCreated == null) {
      if (other.dateCreated != null) {
        return false;
      }
    } else if (!dateCreated.equals(other.dateCreated)) {
      return false;
    }
    if (dateUpdated == null) {
      if (other.dateUpdated != null) {
        return false;
      }
    } else if (!dateUpdated.equals(other.dateUpdated)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (jobId == null) {
      if (other.jobId != null) {
        return false;
      }
    } else if (!jobId.equals(other.jobId)) {
      return false;
    }
    if (lastPluginRan == null) {
      if (other.lastPluginRan != null) {
        return false;
      }
    } else if (!lastPluginRan.equals(other.lastPluginRan)) {
      return false;
    }
    if (lastPluginRanState != other.lastPluginRanState) {
      return false;
    }
    if (objectId == null) {
      if (other.objectId != null) {
        return false;
      }
    } else if (!objectId.equals(other.objectId)) {
      return false;
    }
    if (report == null) {
      if (other.report != null) {
        return false;
      }
    } else if (!report.equals(other.report)) {
      return false;
    }
    return true;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public String getAipId() {
    return aipId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getLastPluginRan() {
    return lastPluginRan;
  }

  public void setLastPluginRan(String lastPluginRan) {
    this.lastPluginRan = lastPluginRan;
  }

  public PluginState getLastPluginRanState() {
    return lastPluginRanState;
  }

  public void setLastPluginRanState(PluginState lastPluginRanState) {
    this.lastPluginRanState = lastPluginRanState;
  }

  public Report getReport() {
    return report;
  }

  public void setReport(Report report) {
    this.report = report;
  }

  @Override
  public String toString() {
    return "JobReport [id=" + id + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", jobId=" + jobId
      + ", aipId=" + aipId + ", objectId=" + objectId + ", lastPluginRan=" + lastPluginRan + ", lastPluginRanState="
      + lastPluginRanState + ", report=" + report + "]";
  }

}
