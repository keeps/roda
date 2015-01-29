/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.client;

import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.SearchException;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;

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

			SearchServiceAsync instance = (SearchServiceAsync) GWT
					.create(SearchService.class);
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
	public SearchResult basicSearch(String query, int hitPageStart,
			int hitPageSize, int snippetsMax, int fieldMaxLength)
			throws RODAException;

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
	public SearchResult advancedSearch(SearchParameter[] searchParameters,
			int hitPageStart, int hitPageSize, int snippetsMax,
			int fieldMaxLength) throws RODAException;

}
