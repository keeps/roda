/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.client;

import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;

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
	public void basicSearch(String query, int hitPageStart,
			int hitPageSize, int snippetsMax, int fieldMaxLength, AsyncCallback<SearchResult> callback);

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
	public void advancedSearch(SearchParameter[] searchParameters,
			int hitPageStart, int hitPageSize, int snippetsMax,
			int fieldMaxLength, AsyncCallback<SearchResult> callback);

}
