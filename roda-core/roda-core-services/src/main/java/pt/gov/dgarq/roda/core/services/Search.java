package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.SearchException;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.fedora.gsearch.FedoraGSearch;
import pt.gov.dgarq.roda.core.fedora.gsearch.FedoraGSearchException;

/**
 * This class implements the Search service.
 * 
 * @author Rui Castro
 */
public class Search extends RODAWebService {
	static final private Logger logger = Logger.getLogger(Search.class);
	/**
	 * Constructs a new instance of Search service.
	 * 
	 * @throws RODAServiceException
	 */
	public Search() throws RODAServiceException {

		super();
		logger.info(getClass().getSimpleName() + " init OK");
	}

	/**
	 * Search for description objects using the query specified as argument.
	 * 
	 * @param query
	 *            the query
	 * @param firstResultIndex
	 *            the index of the first result. Indexes start at 0.
	 * @param maxResults
	 *            the maximum number of results to return.
	 * @param snippetsMax
	 *            the number of snippets in the result fields.
	 * @param fieldMaxLength
	 *            the maximum size of the fields to return.
	 * 
	 * @return a {@link SearchResult} with the results of this search.
	 * 
	 * @throws SearchException
	 * 
	 */
	public SearchResult basicSearch(String query, int firstResultIndex,
			int maxResults, int snippetsMax, int fieldMaxLength)
			throws SearchException {

		try {

			Date start = new Date();
			SearchResult result = getFedoraGSearch().basicSearch(query,
					firstResultIndex, maxResults, snippetsMax, fieldMaxLength);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Search.basicSearch", new String[] { "query", query,
					"firstResultIndex", Integer.toString(firstResultIndex),
					"snippetsMax", Integer.toString(maxResults), "snippetsMax",
					Integer.toString(snippetsMax), "fieldMaxLength",
					Integer.toString(fieldMaxLength) },
					"User %username% called method Search.basicSearch(" + query
							+ ", " + firstResultIndex + ", " + maxResults
							+ ", " + snippetsMax + ", " + fieldMaxLength + ")",
					duration);

			return result;

		} catch (FedoraGSearchException e) {
			throw new SearchException(e.getMessage(), e);
		}

	}

	/**
	 * Search for description objects using the {@link SearchParameter}s given
	 * as argument to construct the query.
	 * 
	 * @param searchParameters
	 *            the {@link SearchParameter}s.
	 * @param firstResultIndex
	 *            the index of the first result. Indexes start at 0.
	 * @param maxResults
	 *            the maximum number of results to return.
	 * @param snippetsMax
	 *            the number of snippets in the result fields.
	 * @param fieldMaxLength
	 *            the maximum size of the fields to return.
	 * 
	 * @return a {@link SearchResult} with the results of this search.
	 * 
	 * @throws SearchException
	 * 
	 */
	public SearchResult advancedSearch(SearchParameter[] searchParameters,
			int firstResultIndex, int maxResults, int snippetsMax,
			int fieldMaxLength) throws SearchException {

		try {

			Date start = new Date();
			SearchResult result = getFedoraGSearch().advancedSearch(
					searchParameters, firstResultIndex, maxResults,
					snippetsMax, fieldMaxLength);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Search.advancedSearch", new String[] {
					"searchParameters",
					Arrays.asList(searchParameters).toString(),
					"firstResultIndex", Integer.toString(firstResultIndex),
					"snippetsMax", Integer.toString(maxResults), "snippetsMax",
					Integer.toString(snippetsMax), "fieldMaxLength",
					Integer.toString(fieldMaxLength) },
					"User %username% called method Search.advancedSearch("
							+ Arrays.asList(searchParameters) + ", "
							+ firstResultIndex + ", " + maxResults + ", "
							+ snippetsMax + ", " + fieldMaxLength + ")",
					duration);
			return result;

		} catch (FedoraGSearchException e) {
			throw new SearchException(e.getMessage(), e);
		}

	}

	private FedoraGSearch getFedoraGSearch() throws SearchException {

		User clientUser = getClientUser();
		FedoraGSearch fedoraGSearch = null;
		if (clientUser != null) {

			try {

				String fedoraGSearchURL = getConfiguration().getString(
						"fedoraGSearchURL");
				fedoraGSearch = new FedoraGSearch(new URL(fedoraGSearchURL),clientUser);
			} catch (MalformedURLException e) {

				logger
						.error(
								"MalformedURLException in FedoraGSearch service URL",
								e);

				throw new SearchException(
						"Malformed URL Exception in FedoraGSearch service URL - "
								+ e.getMessage(), e);
			} catch (FedoraGSearchException e) {
				throw new SearchException(
						"Error creating FedoraGSearch service client - "
								+ e.getMessage(), e);
			}
			return fedoraGSearch;

		} else {

			throw new SearchException("User credentials are not available.");
		}

	}

}
