package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for Browser service.
 * 
 * @author Rui Castro
 */
public class BrowserSDOTest {

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
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);

			} else {
				System.err
						.println(BrowserSDOTest.class.getSimpleName()
								+ " protocol://hostname:port/core-service [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Number of Fonds");
			System.out.println("**************************************");

			int count = browserService
					.getSimpleDescriptionObjectCount(SimpleDescriptionObject.FONDS_FILTER);
			System.out.println(count + " fonds in the repository");

			System.out.println("\n**************************************");
			System.out.println("Number of Fonds (Inactive)");
			System.out.println("**************************************");

			Filter filterActiveFonds = new Filter(
					SimpleDescriptionObject.FONDS_FILTER);
			filterActiveFonds.add(new SimpleFilterParameter("state",
					RODAObject.STATE_INACTIVE));

			count = browserService
					.getSimpleDescriptionObjectCount(filterActiveFonds);
			System.out.println(count + " inactive fonds in the repository");

			System.out.println("\n**************************************");
			System.out.println("List of Fonds");
			System.out.println("**************************************");

			SimpleDescriptionObject[] fondsList = browserService
					.getSimpleDescriptionObjects(new ContentAdapter(
							SimpleDescriptionObject.FONDS_FILTER, null, null));

			for (int i = 0; fondsList != null && i < fondsList.length; i++) {
				System.out.println(fondsList[i]);
			}

			if (fondsList != null && fondsList.length > 0) {

				System.out
						.println("\n*********************************************");
				System.out
						.println("Getting DescriptionObject of the first Fonds ("
								+ fondsList[0].getPid() + ")");
				System.out
						.println("*********************************************");

				DescriptionObject dObject = browserService
						.getDescriptionObject(fondsList[0].getPid());
				System.out.println(dObject);

				int indexToTest = (int) (Math.random() * fondsList.length);

				System.out.println("\n**************************************");
				System.out.println("Fonds index for fonds "
						+ fondsList[indexToTest].getLabel() + " ("
						+ indexToTest + ")");
				System.out.println("**************************************");

				int collectionIndex = browserService
						.getSimpleDescriptionObjectIndex(fondsList[indexToTest]
								.getPid(), new ContentAdapter(
								SimpleDescriptionObject.FONDS_FILTER, null,
								null));

				System.out.print("Index of fonds(" + indexToTest + ") is "
						+ collectionIndex + ". ");
				if (indexToTest == collectionIndex) {
					System.out.println("OK");
				} else {
					System.out.println("FAILED");
				}

			}

			// FIXME commented because static description levels doesn't exist
			// anymore - START
			// System.out.println("\n**************************************");
			// System.out.println("Number of Items (D)");
			// System.out.println("**************************************");
			//
			// Filter itemFilter = new Filter(new SimpleFilterParameter("level",
			// DescriptionLevel.ITEM.getLevel()));
			//
			// count =
			// browserService.getSimpleDescriptionObjectCount(itemFilter);
			// System.out.println(count + " items in the repository");
			//
			// System.out.println("\n**************************************");
			// System.out.println("List of Items (D)");
			// System.out.println("**************************************");
			//
			// SimpleDescriptionObject[] simpleDOs = browserService
			// .getSimpleDescriptionObjects(new ContentAdapter(itemFilter,
			// null, null));
			//
			// if (simpleDOs != null) {
			// for (SimpleDescriptionObject simpleDO : simpleDOs) {
			// System.out.println(simpleDO);
			// }
			// }
			// FIXME commented because static description levels doesn't exist
			// anymore - END

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				System.err.println("Cause exception:");
				e.getCause().printStackTrace();
			}
		}
	}
}
