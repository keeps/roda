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

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.common.RODAObjectList;
import org.roda.core.data.v2.index.facet.FacetFieldResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "index_result")
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexResultWrapper implements Serializable {

  private static final long serialVersionUID = -7896294396414765557L;

  private long offset;
  private long limit;
  private long totalCount;
  private RODAObjectList<?> results;
  private List<FacetFieldResult> facetResults;
  private Date date;

  public IndexResultWrapper() {
    super();
    date = new Date();
  }

  public IndexResultWrapper(long offset, long limit, long totalCount, RODAObjectList<?> results,
    List<FacetFieldResult> facetResults, Date date) {
    super();
    this.offset = offset;
    this.limit = limit;
    this.totalCount = totalCount;
    this.results = results;
    this.facetResults = facetResults;
    this.date = date;
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
  public RODAObjectList<?> getResults() {
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

  public void setResults(RODAObjectList<?> results) {
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
