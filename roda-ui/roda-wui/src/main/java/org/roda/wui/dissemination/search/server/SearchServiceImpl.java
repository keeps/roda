/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.dissemination.search.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.roda.common.RodaCoreFactory;
import org.roda.core.common.RODAException;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.SearchParameter;
import org.roda.core.data.SearchResult;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.wui.common.client.GenericException;
import org.roda.wui.dissemination.search.client.SearchField;
import org.roda.wui.dissemination.search.client.SearchService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.Messages;

/**
 * Search service implementation
 * 
 * @author Luis Faria
 * 
 */
public class SearchServiceImpl extends RemoteServiceServlet implements SearchService {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  static final private Logger logger = Logger.getLogger(SearchServiceImpl.class);

  // protected Search getSearch() throws LoginException, RODAClientException {
  // Search search;
  //
  // search =
  // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession()).getSearchService();
  //
  // return null;
  // }

  public IndexResult<SimpleDescriptionObject> basicSearch(String query, int startIndex, int limit, int snippetsMax,
    int fieldMaxLength) throws RODAException {

    IndexResult<SimpleDescriptionObject> result = null;
    IndexService indexService = RodaCoreFactory.getIndexService();

    try {
      Sublist sublist = new Sublist(startIndex, limit);
      // FIXME define facets
      Facets facets = null;
      result = indexService.find(SimpleDescriptionObject.class, getBasicSearchFilter(null, query), null, sublist,
        facets);
    } catch (IndexServiceException e) {
      logger.error("error", e);
    }
    return result;
  }

  protected Filter getBasicSearchFilter(Filter filter, String query) {
    if (filter == null) {
      filter = new Filter();
    }
    filter.add(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, query));
    return filter;
  }

  public SearchResult advancedSearch(SearchParameter[] searchParameters, int hitPageStart, int hitPageSize,
    int snippetsMax, int fieldMaxLength) throws RODAException {
    // SearchResult result;
    // try {
    // logger.debug("Searching in " + Arrays.asList(searchParameters));
    // result = getSearch().advancedSearch(searchParameters, hitPageStart,
    // hitPageSize, snippetsMax,
    // fieldMaxLength);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;
  }

  // FIXME see if there is a best way to deal with "hierarchical" keys
  // FIXME deal with non-configured/badly-configured keys
  public List<SearchField> getSearchFields(String localeString) throws GenericException {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    String fieldsNamesString = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields");
    if (fieldsNamesString != null) {
      Messages messages = RodaCoreFactory.getI18NMessages(new Locale(localeString));
      String[] fields = fieldsNamesString.split(",");
      for (String field : fields) {
        SearchField searchField = new SearchField();
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "field");
        String fieldType = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "type");
        String fieldLabelI18N = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "i18n");

        searchField.setField(fieldName);
        searchField.setType(fieldType);
        searchField.setLabel(messages.getTranslation(fieldLabelI18N));

        searchFields.add(searchField);
      }
    }
    return searchFields;
  }

}
