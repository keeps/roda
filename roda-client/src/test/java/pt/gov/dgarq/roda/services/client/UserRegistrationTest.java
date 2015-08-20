package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.core.stubs.UserRegistration;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class UserRegistrationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// http://localhost:8180/
		String hostUrl = args[0];
		String username = args[1];
		String password = args[2];
		String casURL = args[3];
		String coreURL = args[4];
		String serviceURL = args[5];
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

			RODAClient rodaClient = new RODAClient(new URL(hostUrl), username,
					password,casUtility);

			UserRegistration userRegistrationService = rodaClient
					.getUserRegistrationService();

			User myUser = rodaClient.getAuthenticatedUser();
			System.out.println("My user: " + myUser);

			/*
			 * Register a new user
			 */
			User newUser = new User("new-user");
			newUser.setFullName("New User");
			newUser.setEmail("new.user@roda.pt");

			System.out.println("\n***********************************");
			System.out.println("Register new user " + newUser.getName()
					+ " ==> ");
			System.out.println("***********************************");

			User newUserUnconfirmed = userRegistrationService.registerUser(
					newUser, "pass");
			System.out.println("New registed user: " + newUserUnconfirmed);

			/*
			 * Modify email before email confirmation.
			 */

			System.out.println("\n***********************************");
			System.out.println("Modify email before confirm "
					+ newUser.getName() + " ==> ");
			System.out.println("***********************************");

			User modifiedUserUnconfirmed = userRegistrationService
					.modifyUnconfirmedEmail(newUserUnconfirmed.getName(),
							"new.user.2@roda.pt");

			System.out.println("Modified email user: "
					+ modifiedUserUnconfirmed);

			/*
			 * Confirm user email
			 */
			System.out.println("\n***********************************");
			System.out.println("Confirm user email "
					+ modifiedUserUnconfirmed.getName() + " ==> "
					+ modifiedUserUnconfirmed.getEmail());
			System.out.println("***********************************");

			User confirmedUser = userRegistrationService.confirmUserEmail(
					modifiedUserUnconfirmed.getName(), modifiedUserUnconfirmed
							.getEmail(), modifiedUserUnconfirmed
							.getEmailConfirmationToken());

			System.out.println("Confirmed User: " + confirmedUser);

			/*
			 * Modify email AFTER email confirmation.
			 */
			System.out.println("\n***********************************");
			System.out.println("Confirm user email " + confirmedUser.getName()
					+ " ==> " + confirmedUser.getEmail());
			System.out.println("***********************************");

			try {

				User user = userRegistrationService.confirmUserEmail(
						confirmedUser.getName(), modifiedUserUnconfirmed
								.getEmail(), modifiedUserUnconfirmed
								.getEmailConfirmationToken());

				System.out.println("No exception throwned. ERROR");
				System.out.println("User returned: " + user);

			} catch (InvalidTokenException e) {
				System.out.println("InvalidTokenException thrown - "
						+ e.getMessage() + ". OK");
			} catch (Exception e) {
				System.out.println("Other Exception thrown - "
						+ e.getClass().getSimpleName() + " - " + e.getMessage()
						+ ". ERROR");
			}

			/*
			 * Ask for password reset
			 */
			System.out.println("\n***********************************");
			System.out.println("Password reset for " + newUser.getName()
					+ " ==> ");
			System.out.println("***********************************");

			User userToReset = userRegistrationService.requestPasswordReset(
					newUser.getName(), null);

			System.out.println("User to reset: " + userToReset);
			System.out.println("Reset token: "
					+ userToReset.getResetPasswordToken() + ", "
					+ userToReset.getResetPasswordTokenExpirationDate());

			/*
			 * Modify user password with token
			 */

			System.out.println("\n***********************************");
			System.out.println("Modify user password for "
					+ userToReset.getName() + " with token "
					+ userToReset.getResetPasswordToken());
			System.out.println("***********************************");

			User resetedUserPassword = userRegistrationService
					.resetUserPassword(userToReset.getName(), "newpass",
							userToReset.getResetPasswordToken());

			System.out.println("User password reset successfull: "
					+ resetedUserPassword);

			/*
			 * Login with new password
			 */

			System.out.println("\n***********************************");
			System.out.println("Login user " + resetedUserPassword.getName()
					+ " with new password");
			System.out.println("***********************************");

			//User authenticatedUser = rodaClient.getLoginService().getAuthenticatedUser(userToReset.getName(), "newpass");

			//System.out.println("Login successfull with new password: "+ authenticatedUser);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
