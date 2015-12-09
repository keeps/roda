/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.Report;

@XmlRootElement
public class JobReport implements Serializable {
  private static final long serialVersionUID = -2028521062931876576L;

  public enum PluginState {
    OK, ERROR
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

  public boolean equals(Object obj) {
    if (obj instanceof JobReport) {
      JobReport other = (JobReport) obj;
      return getId().equals(other.getId());
    } else {
      return false;
    }
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
