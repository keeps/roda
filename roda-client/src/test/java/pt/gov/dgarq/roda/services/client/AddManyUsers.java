package pt.gov.dgarq.roda.services.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.stubs.UserManagement;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class AddManyUsers {
	static final private Logger logger = Logger.getLogger(AddManyUsers.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 7) {
			System.err.println("Wrong number of arguments.");
			System.err.println("Use " + AddManyUsers.class.getSimpleName()
					+ "protocol://host:port/ casURL coreURL serviceURL (number of users)");
		} else {
			// http://localhost:8180/
			String hostUrl = args[0];
			String serviceusername = args[1];
			String servicepassword = args[2];
			String casURL = args[3];
			String coreURL = args[4];
			String serviceURL = args[5];
			int numberOfUsers = Integer.parseInt(args[6]);

			RODAClient rodaClient = null;
			try {
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

				rodaClient = new RODAClient(new URL(hostUrl), serviceusername,
						servicepassword,casUtility);
			} catch (RODAClientException e) {
				logger.error("Error creating RODA client - " + e.getMessage(),
						e);
				System.exit(1);
			} catch (MalformedURLException e) {
				logger.error("Invalid URL exception", e);
				System.exit(1);
			} catch (LoginException e) {
				logger.error("Invalid login exception", e);
				System.exit(1);
			}

			UserManagement userManagement = null;
			try {
				userManagement = rodaClient.getUserManagementService();
			} catch (RODAClientException e) {
				logger.error(
						"Service exception accessing UserManagement service - "
								+ e.getMessage(), e);
				System.exit(1);
			}

			long timeInMillisBeforeStart = Calendar.getInstance()
					.getTimeInMillis();

			for (int i = 0; i < numberOfUsers; i++) {

				String username = "user" + i;
				User user = new User(username);
				user.setFullName("Test User" + i);
				user.setEmail(username + "@roda.dgarq.gov.pt");
				user.setLocalityName("Lisboa");
				user.setCountryName("Portugal");

				try {

					userManagement.addUser(user);

					logger.info("User " + username + " created successfully");

				} catch (UserAlreadyExistsException e) {
					logger.error("User " + username
							+ " already exists. Ignoring");
				} catch (UserManagementException e) {
					logger.error("Internal service exception. Ignoring.", e);
				} catch (RemoteException e) {
					logger.error("Remote exception. Ignoring", e);
				} catch (EmailAlreadyExistsException e) {
					logger.error("Email " + user.getEmail()
							+ " already exists. Ignoring");
				}
			}

			long timeInMillisAfterFinish = Calendar.getInstance()
					.getTimeInMillis();

			long durationInMillis = timeInMillisAfterFinish
					- timeInMillisBeforeStart;

			long durationInSeconds = durationInMillis / 1000l;

			logger.info("Ingest " + numberOfUsers + " users in "
					+ durationInSeconds + " seconds");
		}

	}
}
