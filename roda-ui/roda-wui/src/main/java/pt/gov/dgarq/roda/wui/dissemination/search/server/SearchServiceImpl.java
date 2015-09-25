package pt.gov.dgarq.roda.wui.dissemination.search.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import pt.gov.dgarq.roda.wui.dissemination.search.client.SearchField;
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
  
  
  
  /*

Exemplo

search.fields = scope,producer,author,location,arrangement

search.fields.scope.field = scope
search.fields.scope.label.pt_PT = Contexto
search.fields.scope.label.en_EN = Context
search.fields.scope.type = text

search.fields.producer.field = producer
search.fields.producer.label.pt_PT = Produtor
search.fields.producer.label.en_EN = Producer
search.fields.producer.type = text

search.fields.author.field = author
search.fields.author.label.pt_PT = Autor
search.fields.author.label.en_EN = Author
search.fields.author.type = text

search.fields.location.field = location
search.fields.location.label.pt_PT = Localiza\u00E7\u00E3o
search.fields.location.label.en_EN = Location
search.fields.location.type = text

search.fields.arrangement.field = arrangement
search.fields.arrangement.label.pt_PT = Organiza\u00E7\u00E3o
search.fields.arrangement.label.en_EN = Arrangement
search.fields.arrangement.type = text
   */
  public List<SearchField> getSearchFields() throws IOException {
    try{
      Properties searchFieldsProperties = new Properties();
      searchFieldsProperties.load(RodaCoreFactory.getConfigurationFile("roda-wui.properties"));
      List<SearchField> searchFields = new ArrayList<SearchField>();
      String fieldsNamesString = searchFieldsProperties.getProperty("search.fields");
      if(fieldsNamesString!=null){
        String[] fields = fieldsNamesString.split(",");
        for(String field : fields){
          SearchField searchField = new SearchField();
          searchField.setField((searchFieldsProperties.getProperty("search.fields."+field+".field")==null)?field:searchFieldsProperties.getProperty("search.fields."+field+".field"));
          Map<String,String> labels = new HashMap<String,String>();
          labels.put("pt_PT",(searchFieldsProperties.getProperty("search.fields."+field+".label.pt_PT")==null)?field:searchFieldsProperties.getProperty("search.fields."+field+".label.pt_PT"));
          labels.put("en_EN",(searchFieldsProperties.getProperty("search.fields."+field+".label.en_EN")==null)?field:searchFieldsProperties.getProperty("search.fields."+field+".label.en_EN"));
          searchField.setLabels(labels);
          searchField.setType((searchFieldsProperties.getProperty("search.fields."+field+".type")==null)?field:searchFieldsProperties.getProperty("search.fields."+field+".type"));
          searchFields.add(searchField);
        }
      }
      return searchFields;
    }catch(IOException e){
      logger.error(e.getMessage(),e);
      throw e;
    }catch(Throwable t){
      logger.error(t.getMessage(),t);
      return null;
    }
  }

}
