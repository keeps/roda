package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for Browser service.
 * 
 * @author Rui Castro
 */
public class BrowserSimpleROTest {

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
						.println(BrowserSimpleRepresentationPOTest.class
								.getSimpleName()
								+ " protocol://hostname:port/core-service [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Number of Representations");
			System.out.println("**************************************");

			int count = browserService.getSimpleRepresentationObjectCount(null);
			System.out.println(count + " representations in the repository");

			System.out.println("\n**************************************");
			System.out.println("Number of Representations (Inactive)");
			System.out.println("**************************************");

			Filter filterInactive = new Filter();
			filterInactive.add(new SimpleFilterParameter("state",
					RODAObject.STATE_INACTIVE));

			count = browserService
					.getSimpleRepresentationObjectCount(filterInactive);
			System.out.println(count
					+ " inactive representations in the repository");

			SimpleRepresentationObject[] representationsList = browserService
					.getSimpleRepresentationObjects(null);

			System.out.println("\n**************************************");
			System.out.println("List of Representations");
			System.out.println("**************************************");

			for (int i = 0; representationsList != null
					&& i < representationsList.length; i++) {
				System.out.println(representationsList[i]);
			}

			if (representationsList != null && representationsList.length > 0) {

				System.out
						.println("\n*********************************************");
				System.out
						.println("Getting RepresentationObject of the first representation ("
								+ representationsList[0].getPid() + ")");
				System.out
						.println("*********************************************");

				RepresentationObject rObject = browserService
						.getRepresentationObject(representationsList[0]
								.getPid());
				System.out.println(rObject);

			}

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				System.err.println("Cause exception:");
				e.getCause().printStackTrace();
			}
		}
	}
}
