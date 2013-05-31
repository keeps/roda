package pt.gov.dgarq.roda.core.fedora.gsearch;

import java.net.URL;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.search.DateRangeSearchParameter;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;

/**
 * @author Rui Castro
 */
public class FedoraGSearchTest {
	static final private Logger logger = Logger
			.getLogger(FedoraGSearchTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String serviceURL = null;
		String query = "ead.unittitle:minho";

		if (args.length == 0) {

			System.err.println("Wrong number of arguments.\n\tUse "
					+ FedoraGSearch.class.getSimpleName()
					+ " serviceURL [query]");
			System.exit(1);

		} else {

			serviceURL = args[0];

			if (args.length > 1) {
				query = args[1];
			}
			// no query specified
		}

		try {

			FedoraGSearch gsearch = new FedoraGSearch(new URL(serviceURL),
					new User("guest"));

			System.out.println("\n********************************");
			System.out.println("Searching '" + query + "'");
			System.out.println("********************************");

			SearchResult searchResult = null;

			searchResult = gsearch.basicSearch(query, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching with parameters");
			System.out.println("********************************");

			SearchParameter parameter1 = new DefaultSearchParameter(
					new String[] { "ead.scopecontent" }, "dinheiro",
					DefaultSearchParameter.MATCH_EXACT_PHRASE);

			SearchParameter parameter2 = new DefaultSearchParameter(
					new String[] { "ead.otherlevel" }, "DC SR",
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);

			SearchParameter parameter3 = new DateRangeSearchParameter(
					"ead.unitdate", "18920218", "19120517");

			searchResult = gsearch.advancedSearch(
					new SearchParameter[] { parameter3 }, 0, 3, 50, 500);

			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching with empty query");
			System.out.println("********************************");

			searchResult = gsearch.basicSearch("", 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching with null query");
			System.out.println("********************************");

			searchResult = gsearch.basicSearch("", 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching for results with #quot;");
			System.out.println("********************************");

			parameter1 = new DefaultSearchParameter(
					new String[] { "ead.unitid" }, "0335",
					DefaultSearchParameter.MATCH_EXACT_PHRASE);
			parameter2 = new DefaultSearchParameter(
					new String[] { "ead.unittitle" }, "descontadas",
					DefaultSearchParameter.MATCH_EXACT_PHRASE);

			searchResult = gsearch.advancedSearch(new SearchParameter[] {
					parameter1, parameter2 }, 0, 1, 50, 500);
			showResults(searchResult);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showResults(SearchResult search) {

		if (search == null) {
			logger.info("search service returned null");
		} else {

			logger.info("SearchResult (" + search.getDatetime() + ", "
					+ search.getIndexName() + ", " + search.getHitTotal()
					+ ", " + search.getHitPageSize() + ", "
					+ search.getHitPageStart() + ", " + search.getResultCount()
					+ " results)");

			SearchResultObject[] searchResults = search
					.getSearchResultObjects();

			if (searchResults == null || searchResults.length == 0) {

				logger.info("query didn't return any results");

			} else {

				logger.info("query returned " + searchResults.length
						+ " results");

				for (int i = 0; searchResults != null
						&& i < searchResults.length; i++) {

					System.out.println(searchResults[i]);

				}
			}
		}
	}
}
