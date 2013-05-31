package pt.gov.dgarq.roda.core.services;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.search.DateRangeSearchParameter;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;

/**
 * @author Rui Castro
 */
public class SearchTest {
	static final private Logger logger = Logger.getLogger(SearchTest.class);

	public static void main(String[] args) {

		String query = "banco";

		if (args.length > 0) {
			query = args[0];
		} else {
			// no query specified
		}

		try {

			Search searchService = new Search();

			System.out.println("********************************");
			System.out.println("Searching '" + query + "'");
			System.out.println("********************************");

			SearchResult searchResult = null;

			searchResult = searchService.basicSearch(query, 0, 3, 50, 500);
			showResults(searchResult);

			System.out.println("\n********************************");
			System.out.println("Searching with parameters");
			System.out.println("********************************");

			SearchParameter parameter1 = new DefaultSearchParameter(
					new String[] { EadcSearchFields.UNITTITLE,
							EadcSearchFields.SCOPECONTENT,
							EadcSearchFields.BIOGHIST }, "Registo de entrada",
					DefaultSearchParameter.MATCH_EXACT_PHRASE);

			SearchParameter parameter2 = new DefaultSearchParameter(
					new String[] { EadcSearchFields.LEVEL },
					DescriptionLevel.ITEM.getLevel(),
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);

			// 1892-02-18/1912-05-17
			SearchParameter parameter3 = new DateRangeSearchParameter(
					"ead.unitdate", "19120518", "19991231");

			searchResult = searchService.advancedSearch(new SearchParameter[] {
					parameter1, parameter2 }, 0, 3, 50, 500);

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
