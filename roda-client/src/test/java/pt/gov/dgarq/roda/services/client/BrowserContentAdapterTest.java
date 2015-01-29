package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for Browser Content Adapter.
 * 
 * @author Rui Castro
 */
public class BrowserContentAdapterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;

			if (args.length == 6) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl), username, password,casUtility);
			} else {
				System.err
						.println(BrowserContentAdapterTest.class
								.getSimpleName()
								+ " protocol://hostname:port/core-service [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			/*
			 * Filter contentModel=roda:d
			 */

			Filter filter = new Filter();
			filter.add(new SimpleFilterParameter("contentModel", "roda:d"));
			Sorter sorter = new Sorter();
			sorter.add(new SortParameter("lastModifiedDate", true));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			int count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

			/*
			 * Filter pid=roda:1
			 */

			filter = new Filter();
			filter.add(new SimpleFilterParameter("pid", "roda:1"));
			sorter = new Sorter();
			sorter.add(new SortParameter("lastModifiedDate", true));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

			/*
			 * Filter parentpid=roda:1
			 */

			filter = new Filter();
			filter.add(new SimpleFilterParameter("parentpid", "roda:1"));
			sorter = new Sorter();
			sorter.add(new SortParameter("lastModifiedDate", true));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

			/*
			 * Filter label=TESTE
			 */

			filter = new Filter();
			filter.add(new SimpleFilterParameter("label", "TESTE"));
			sorter = new Sorter();
			sorter.add(new SortParameter("lastModifiedDate", true));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

			/*
			 * Filter level=FONDS
			 */

			filter = new Filter();
			filter.add(new SimpleFilterParameter("level",
					DescriptionLevel.FONDS.getLevel()));
			sorter = new Sorter();
			sorter.add(new SortParameter("lastModifiedDate", true));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

			/*
			 * Filter state=Inactive
			 */

			filter = new Filter();
			filter.add(new SimpleFilterParameter("state",
					SimpleDescriptionObject.STATE_INACTIVE));
			sorter = new Sorter();
			sorter.add(new SortParameter("createdDate", false));

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjectsCount(" + filter
					+ ")");
			System.out.println("**************************************");

			count = browserService.getSimpleDescriptionObjectCount(filter);
			System.out.println(count + " objects");

			System.out.println("\n**************************************");
			System.out.println("getSimpleDescriptionObjects(" + filter + ")");
			System.out.println("**************************************");

			printSDOs(browserService
					.getSimpleDescriptionObjects(new ContentAdapter(filter,
							sorter, null)));

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				System.err.println("Cause exception:");
				e.getCause().printStackTrace();
			}
		}
	}

	private static void printSDOs(SimpleDescriptionObject[] simpleDOs) {
		if (simpleDOs != null) {
			for (SimpleDescriptionObject sdo : simpleDOs) {
				System.out.println(sdo);
			}
		}
	}
}
