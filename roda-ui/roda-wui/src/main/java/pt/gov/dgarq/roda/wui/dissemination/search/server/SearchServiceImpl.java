package pt.gov.dgarq.roda.wui.dissemination.search.server;

import org.apache.log4j.Logger;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.dissemination.search.client.SearchService;

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

}
