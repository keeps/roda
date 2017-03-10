/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "index_result")
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexResult<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = -7896294396414765557L;

  private long offset;
  private long limit;
  private long totalCount;

  private List<T> results;
  private List<FacetFieldResult> facetResults;
  private Date date;

  public IndexResult() {
    super();
    date = new Date();
  }

  public IndexResult(long offset, long limit, long totalCount, List<T> results, List<FacetFieldResult> facetResults) {
    super();
    this.offset = offset;
    this.limit = limit;
    this.totalCount = totalCount;
    this.results = results;
    this.facetResults = facetResults;
    date = new Date();
  }

  /**
   * @return the offset
   */
  public long getOffset() {
    return offset;
  }

  /**
   * @return the limit
   */
  public long getLimit() {
    return limit;
  }

  /**
   * @return the totalCount
   */
  public long getTotalCount() {
    return totalCount;
  }

  /**
   * @return the results
   */
  @XmlElementWrapper(name = "results")
  @XmlElements({

    @XmlElement(name = RodaConstants.CONTROLLER_AIP_PARAM, type = IndexedAIP.class),
    @XmlElement(name = RodaConstants.CONTROLLER_REPRESENTATION_PARAM, type = IndexedRepresentation.class),
    @XmlElement(name = RodaConstants.CONTROLLER_FILE_PARAM, type = IndexedFile.class),

    @XmlElement(name = RodaConstants.CONTROLLER_DIP_PARAM, type = IndexedDIP.class),
    @XmlElement(name = RodaConstants.CONTROLLER_DIP_FILE_PARAM, type = DIPFile.class),

    @XmlElement(name = "preservationAgent", type = IndexedPreservationAgent.class),
    @XmlElement(name = "preservationEvent", type = IndexedPreservationEvent.class),

    @XmlElement(name = "job", type = Job.class), @XmlElement(name = "report", type = IndexedReport.class),
    @XmlElement(name = "log", type = LogEntry.class), @XmlElement(name = "notification", type = Notification.class),

    @XmlElement(name = "risk", type = IndexedRisk.class), @XmlElement(name = "incidence", type = RiskIncidence.class),

    @XmlElement(name = "format", type = Format.class),

    @XmlElement(name = "transferredResource", type = TransferredResource.class),
    @XmlElement(name = "user", type = User.class), @XmlElement(name = "group", type = Group.class),

    @XmlElement(name = "result", type = Object.class)

  })
  public List<T> getResults() {
    return results;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setLimit(long limit) {
    this.limit = limit;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public List<FacetFieldResult> getFacetResults() {
    return facetResults;
  }

  public void setFacetResults(List<FacetFieldResult> facetResults) {
    this.facetResults = facetResults;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return "IndexResult [offset=" + offset + ", limit=" + limit + ", totalCount=" + totalCount + ", results=" + results
      + ", facetResults=" + facetResults + ", date=" + date + "]";
  }

}
