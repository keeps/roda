/**
 * 
 */
package org.roda.wui.dissemination.search.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.roda.common.RodaCoreFactory;
import org.roda.core.common.LoginException;
import org.roda.core.common.RODAClientException;
import org.roda.core.common.RODAException;
import org.roda.core.common.SearchException;
import org.roda.core.data.SearchParameter;
import org.roda.core.data.SearchResult;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.wui.common.client.GenericException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface SearchService extends RemoteService {

  /**
   * Seach service URI
   */
  public static final String SERVICE_URI = "searchservice";

  /**
   * Utilities
   * 
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static SearchServiceAsync getInstance() {

      SearchServiceAsync instance = (SearchServiceAsync) GWT.create(SearchService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

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
  public IndexResult<SimpleDescriptionObject> basicSearch(String query, int hitPageStart, int hitPageSize,
    int snippetsMax, int fieldMaxLength) throws RODAException;

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
  public SearchResult advancedSearch(SearchParameter[] searchParameters, int hitPageStart, int hitPageSize,
    int snippetsMax, int fieldMaxLength) throws RODAException;

  public List<SearchField> getSearchFields(String locale) throws GenericException;

}
