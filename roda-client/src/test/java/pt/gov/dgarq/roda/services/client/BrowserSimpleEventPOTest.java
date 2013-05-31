package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.stubs.Browser;

/**
 * Test class for Browser service.
 * 
 * @author Rui Castro
 */
public class BrowserSimpleEventPOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;

			if (args.length == 3) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);

			} else {
				System.err
						.println(BrowserSimpleROTest.class.getSimpleName()
								+ " protocol://hostname:port/core-service [username password]");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Number of Event Preservation Objects");
			System.out.println("**************************************");

			int count = browserService
					.getSimpleEventPreservationObjectCount(null);
			System.out.println(count
					+ " event preservation objects in the repository");

			System.out.println("\n**************************************");
			System.out
					.println("Number of Event Preservation Objects (Inactive)");
			System.out.println("**************************************");

			Filter filterInactive = new Filter();
			filterInactive.add(new SimpleFilterParameter("state",
					RODAObject.STATE_INACTIVE));

			count = browserService
					.getSimpleEventPreservationObjectCount(filterInactive);
			System.out.println(count
					+ " inactive event preservation objects in the repository");

			SimpleEventPreservationObject[] simpleEPOs = browserService
					.getSimpleEventPreservationObjects(null);

			System.out.println("\n**************************************");
			System.out.println("List of Event Preservation Objects");
			System.out.println("**************************************");

			for (int i = 0; simpleEPOs != null && i < simpleEPOs.length; i++) {
				System.out.println(simpleEPOs[i]);
			}

			if (simpleEPOs != null && simpleEPOs.length > 0) {

				System.out
						.println("\n*********************************************");
				System.out
						.println("Getting EventPreservationObject of the first representation ("
								+ simpleEPOs[0].getPid() + ")");
				System.out
						.println("*********************************************");

				EventPreservationObject rObject = browserService
						.getEventPreservationObject(simpleEPOs[0].getPid());
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
