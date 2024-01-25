/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
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
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_INDEX_RESULT)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexResult<T extends Serializable> implements Serializable {

  @Serial
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

  public void setOffset(long offset) {
    this.offset = offset;
  }

  /**
   * @return the limit
   */
  public long getLimit() {
    return limit;
  }

  public void setLimit(long limit) {
    this.limit = limit;
  }

  /**
   * @return the totalCount
   */
  public long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  /**
   * @return the results
   */
  @XmlElementWrapper(name = "results")
  @XmlElements({@XmlElement(name = RodaConstants.RODA_OBJECT_AIP, type = IndexedAIP.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION, type = IndexedRepresentation.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_FILE, type = IndexedFile.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_DIP, type = IndexedDIP.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_DIPFILE, type = DIPFile.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_PRESERVATION_AGENT, type = IndexedPreservationAgent.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_PRESERVATION_EVENT, type = IndexedPreservationEvent.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_JOB, type = Job.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_REPORT, type = IndexedReport.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_LOG, type = LogEntry.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_NOTIFICATION, type = Notification.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_RISK, type = IndexedRisk.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_INCIDENCE, type = RiskIncidence.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION, type = RepresentationInformation.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_TRANSFERRED_RESOURCE, type = TransferredResource.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_USER, type = User.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_GROUP, type = Group.class),
    @XmlElement(name = RodaConstants.RODA_OBJECT_OTHER, type = Object.class)})
  public List<T> getResults() {
    return results;
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
