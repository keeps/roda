package pt.gov.dgarq.roda.services.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.stubs.Browser;

/**
 * @author Rui Castro
 * 
 */
public class BrowserGetDescriptionObject {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;
			String pid = null;

			if (args.length == 2) {

				// http://localhost:8180/
				String hostUrl = args[0];
				pid = args[1];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 4) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				pid = args[1];
				String username = args[2];
				String password = args[3];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err.println(BrowserGetDescriptionObject.class
						.getSimpleName()
						+ " protocol://hostname:port/ pid [username password]");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Get permissions " + pid);
			System.out.println("**************************************");

			System.out
					.println(browserService.getRODAObjectUserPermissions(pid));

			System.out.println("\n**************************************");
			System.out.println("Get Description Object " + pid);
			System.out.println("**************************************");

			DescriptionObject descriptionObject = browserService
					.getDescriptionObject(pid);
			System.out.println(descriptionObject);

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
		} catch (RODAClientException e) {
			e.printStackTrace();
		}
	}
}
