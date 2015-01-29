package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.core.data.search.DateRangeSearchParameter;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.core.stubs.Search;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class SearchTest {
	static final private Logger logger = Logger.getLogger(SearchTest.class);

	public static void main(String[] args) {

		RODAClient rodaClient = null;

		try {

			String query = "banco";

			if (args.length == 4) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length >= 6) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err
						.println(SearchTest.class.getSimpleName()
								+ " protocol://hostname:port/service [username password] casURL coreURL query]");
				System.exit(1);
			}

			if (args.length > 6) {
				query = args[6];
			} else {
				// no query specified
			}

			Search searchService = rodaClient.getSearchService();
			SearchResult searchResult = null;

			System.out.println("********************************");
			System.out.println("Searching '" + query + "'");
			System.out.println("********************************");

			searchResult = searchService.basicSearch(query, 0, 10, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching with parameters");
			System.out.println("********************************");

			System.out.println("\n********************************");
			System.out.println("Match exact phrase 'Registo de entrada'");
			System.out.println("********************************");

			SearchParameter parameter1 = new DefaultSearchParameter(
					new String[] { "ead.unittitle", "ead.scopecontent",
							"ead.bioghist" }, "Registo de entrada",
					DefaultSearchParameter.MATCH_EXACT_PHRASE);

			searchResult = searchService.advancedSearch(
					new SearchParameter[] { parameter1 }, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Match at least one word 'D DC'");
			System.out.println("********************************");

			SearchParameter parameter2 = new DefaultSearchParameter(
					new String[] { "ead.otherlevel" }, "D DC",
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);

			searchResult = searchService.advancedSearch(
					new SearchParameter[] { parameter2 }, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Match all words 'Banco do Minho'");
			System.out.println("********************************");

			SearchParameter parameter3 = new DefaultSearchParameter(
					new String[] { "ead.unittitle" }, "Banco do Minho",
					DefaultSearchParameter.MATCH_ALL_WORDS);

			searchResult = searchService.advancedSearch(
					new SearchParameter[] { parameter3 }, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Match without words 'Banco'");
			System.out.println("********************************");

			SearchParameter parameter41 = new DefaultSearchParameter(
					new String[] { "ead.unittitle" }, "Minho",
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);
			SearchParameter parameter4 = new DefaultSearchParameter(
					new String[] { "ead.unittitle" }, "Banco",
					DefaultSearchParameter.MATCH_WITHOUT_WORDS);

			searchResult = searchService.advancedSearch(new SearchParameter[] {
					parameter41, parameter4 }, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n***********************************");
			System.out.println("Date range '1912-05-18/1999-12-31'");
			System.out.println("************************************");

			// 1892-02-18/1912-05-17
			SearchParameter parameter5 = new DateRangeSearchParameter(
					"ead.unitdate", "1912-05-18", "1999-12-31");

			searchResult = searchService.advancedSearch(
					new SearchParameter[] { parameter5 }, 0, 3, 50, 500);
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
