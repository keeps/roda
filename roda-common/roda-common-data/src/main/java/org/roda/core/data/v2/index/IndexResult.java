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

import org.roda.core.data.v2.index.facet.FacetFieldResult;

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
    this.setFacetResults(facetResults);
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
