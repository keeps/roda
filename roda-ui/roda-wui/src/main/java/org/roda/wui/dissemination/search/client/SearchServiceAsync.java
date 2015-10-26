/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.dissemination.search.client;

import java.io.IOException;
import java.util.List;

import org.roda.core.data.SearchParameter;
import org.roda.core.data.SearchResult;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SimpleDescriptionObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface SearchServiceAsync {

  /**
   * Make a basic search
   * 
   * @param query
   * @param hitPageStart
   * @param hitPageSize
   * @param snippetsMax
   * @param fieldMaxLength
   * @return
   * @throws RODAException
   */
  public void basicSearch(String query, int hitPageStart, int hitPageSize, int snippetsMax, int fieldMaxLength,
    AsyncCallback<IndexResult<SimpleDescriptionObject>> callback);

  /**
   * Make an advanced search
   * 
   * @param searchParameters
   * @param hitPageStart
   * @param hitPageSize
   * @param snippetsMax
   * @param fieldMaxLength
   * @return
   * @throws RODAException
   */
  public void advancedSearch(SearchParameter[] searchParameters, int hitPageStart, int hitPageSize, int snippetsMax,
    int fieldMaxLength, AsyncCallback<SearchResult> callback);
  
  void getSearchFields(String locale, AsyncCallback<List<SearchField>> callback);

}
