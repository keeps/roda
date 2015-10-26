/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a search result returned by the Search service.
 * 
 * @author Rui Castro
 */
public class SearchResult implements Serializable {
  private static final long serialVersionUID = -4951720994794095475L;

  private String datetime;
  private String indexName;
  private String resultPageXslt;
  private int hitPageStart;
  private int hitTotal;
  private int hitPageSize;
  private int resultCount;

  private List<SearchResultObject> searchResultObjects = new ArrayList<SearchResultObject>();

  /**
   * Constructs a new empty SearchResult
   */
  public SearchResult() {
  }

  /**
   * Constructs a new SearchResult cloning an existing SearchResult.
   * 
   * @param searchResult
   */
  public SearchResult(SearchResult searchResult) {
    this(searchResult.getDatetime(), searchResult.getIndexName(), searchResult.getResultPageXslt(), searchResult
      .getHitTotal(), searchResult.getHitPageSize(), searchResult.getHitPageStart(), searchResult.getResultCount(),
      searchResult.getSearchResultObjects());
  }

  /**
   * Constructs a new SearchResult with the given parameters.
   * 
   * @param datetime
   * @param indexName
   * @param resultPageXslt
   * 
   * @param hitTotal
   * @param hitPageSize
   * @param hitPageStart
   * @param resultCount
   * @param searchResultObjects
   */
  public SearchResult(String datetime, String indexName, String resultPageXslt, int hitTotal, int hitPageSize,
    int hitPageStart, int resultCount, SearchResultObject[] searchResultObjects) {

    setDatetime(datetime);
    setIndexName(indexName);
    setResultPageXslt(resultPageXslt);
    setHitPageStart(hitPageStart);
    setHitTotal(hitTotal);
    setHitPageSize(hitPageSize);
    setResultCount(resultCount);
    setSearchResultObjects(searchResultObjects);

  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SearchResult (" + getDatetime() + ", " + getIndexName() + ", " + getResultPageXslt() + ", " + getHitTotal()
      + ", " + getHitPageStart() + ", " + getHitPageSize() + ", " + getResultCount() + ", " + searchResultObjects + ")";
  }

  /**
   * @return the hitPageStart
   */
  public int getHitPageStart() {
    return hitPageStart;
  }

  /**
   * @param hitPageStart
   *          the hitPageStart to set
   */
  public void setHitPageStart(int hitPageStart) {
    this.hitPageStart = hitPageStart;
  }

  /**
   * @return the hitTotal
   */
  public int getHitTotal() {
    return hitTotal;
  }

  /**
   * @param hitTotal
   *          the hitTotal to set
   */
  public void setHitTotal(int hitTotal) {
    this.hitTotal = hitTotal;
  }

  /**
   * @return the hitPageSize
   */
  public int getHitPageSize() {
    return hitPageSize;
  }

  /**
   * @param hitPageSize
   *          the hitPageSize to set
   */
  public void setHitPageSize(int hitPageSize) {
    this.hitPageSize = hitPageSize;
  }

  /**
   * @return the resultCount
   */
  public int getResultCount() {
    return resultCount;
  }

  /**
   * @param resultCount
   *          the resultCount to set
   */
  public void setResultCount(int resultCount) {
    this.resultCount = resultCount;
  }

  /**
   * @return the resultPageXslt
   */
  public String getResultPageXslt() {
    return resultPageXslt;
  }

  /**
   * @param resultPageXslt
   *          the resultPageXslt to set
   */
  public void setResultPageXslt(String resultPageXslt) {
    this.resultPageXslt = resultPageXslt;
  }

  /**
   * @return the indexName
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * @param indexName
   *          the indexName to set
   */
  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  /**
   * @return the datetime
   */
  public String getDatetime() {
    return datetime;
  }

  /**
   * @param datetime
   *          the datetime to set
   */
  public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  /**
   * @return the searchResultObjects
   */
  public SearchResultObject[] getSearchResultObjects() {
    return (SearchResultObject[]) searchResultObjects.toArray(new SearchResultObject[searchResultObjects.size()]);
  }

  /**
   * @param searchResultObjects
   *          the searchResultObjects to set
   */
  public void setSearchResultObjects(SearchResultObject[] searchResultObjects) {
    this.searchResultObjects.clear();
    if (searchResultObjects != null) {
      this.searchResultObjects.addAll(Arrays.asList(searchResultObjects));
    }
  }

  /**
   * Adds a {@link SearchResultObject} to the list of result objects.
   * 
   * @param searchResultObject
   */
  public void addSearchResultObject(SearchResultObject searchResultObject) {
    this.searchResultObjects.add(searchResultObject);
  }

}
