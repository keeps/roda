package pt.gov.dgarq.roda.wui.dissemination.search.server;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.stubs.Search;
import pt.gov.dgarq.roda.wui.dissemination.search.client.SearchService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Search service implementation
 * 
 * @author Luis Faria
 * 
 */
public class SearchServiceImpl extends RemoteServiceServlet implements
		SearchService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final private Logger logger = Logger
			.getLogger(SearchServiceImpl.class);

	protected Search getSearch() throws LoginException, RODAClientException {
		Search search;

		search = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getSearchService();

		return search;
	}

	public SearchResult basicSearch(String query, int startIndex, int limit,
			int snippetsMax, int fieldMaxLength) throws RODAException {
		SearchResult result;

		try {
			result = getSearch().basicSearch(query, startIndex, limit,
					snippetsMax, fieldMaxLength);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return result;
	}

	public SearchResult advancedSearch(SearchParameter[] searchParameters,
			int hitPageStart, int hitPageSize, int snippetsMax,
			int fieldMaxLength) throws RODAException {
		SearchResult result;
		try {
			logger.debug("Searching in " + Arrays.asList(searchParameters));
			result = getSearch().advancedSearch(searchParameters, hitPageStart,
					hitPageSize, snippetsMax, fieldMaxLength);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return result;
	}

}
