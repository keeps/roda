package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class LoginTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// http://localhost:8180/roda-core
		String hostUrl = args[0];
		String username = args[1];
		String password = args[2];
		String casURL = args[3];
		String coreURL = args[4];
		String serviceURL = args[5];

		/*
		 * Guest login
		 */
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

			RODAClient rodaGuestClient = new RODAClient(new URL(hostUrl),casUtility);

			User authenticatedGuestUser = rodaGuestClient
					.getAuthenticatedUser();

			System.out.println("Guest client login successful: "
					+ authenticatedGuestUser);
			// System.out.println("Guest client login successful: "
			// + authenticatedGuestUser.getFullName() + " ("
			// + authenticatedGuestUser.getName() + ")");

		} catch (Exception e) {
			System.out.println("Guest client login failed.");
			e.printStackTrace();
		}

		/*
		 * authenticated login
		 */
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

			RODAClient rodaGuestClient = new RODAClient(new URL(hostUrl),
					username, password,casUtility);

			User authenticatedUser = rodaGuestClient.getAuthenticatedUser();

			System.out.println("Authenticated client login successful: "
					+ authenticatedUser);

		} catch (Exception e) {
			System.out.println("Authenticated client login failed.");
			e.printStackTrace();
		}

		/*
		 * authenticated login to fail
		 */

		RODAClient rodaGuestClient;
		try {

			try {
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

				rodaGuestClient = new RODAClient(new URL(hostUrl),
						"fakeusername", "invalidpassword",casUtility);

				User authenticatedUser = rodaGuestClient.getAuthenticatedUser();

				System.out.println("Fake client ("
						+ authenticatedUser.getName()
						+ ") login successful. FAILED");

			} catch (LoginException e) {
				System.out
						.println("Fake client login throwed LoginException. OK");
				// e.printStackTrace(System.out);
			}

		} catch (Exception e) {
			System.out.println("Fake client login failed.");
			e.printStackTrace();
		}

	}

}
