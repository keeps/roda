package pt.gov.dgarq.roda.services.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.stubs.Browser;

/**
 * @author Rui Castro
 * 
 */
public class BrowserDownloadTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;
			String doPID = null;

			if (args.length == 2) {

				// http://localhost:8180/
				String hostUrl = args[0];
				doPID = args[1];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length == 4) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				doPID = args[3];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err
						.println(BrowserTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] doPID");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Get original representation " + doPID);
			System.out.println("**************************************");

			RepresentationObject representationObject = browserService
					.getDOOriginalRepresentation(doPID);
			System.out.println(representationObject);

		} catch (RODAClientException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (NoSuchRODAObjectException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (BrowserException e) {
			e.printStackTrace();
		}
	}
}
