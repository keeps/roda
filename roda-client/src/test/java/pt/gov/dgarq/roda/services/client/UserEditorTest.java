package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.stubs.UserEditor;

/**
 * @author Rui Castro
 * 
 */
public class UserEditorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// http://localhost:8180/
		String hostUrl = args[0];
		String username = args[1];
		String password = args[2];

		try {

			RODAClient rodaClient = new RODAClient(new URL(hostUrl), username,
					password);

			UserEditor userEditor = rodaClient.getUserEditorService();

			User myUser = rodaClient.getAuthenticatedUser();
			System.out.println("My user: " + myUser);

			myUser.setCountryName("Espanha");
			myUser.setFullName("Administrador (6)Demo");

			/*
			 * Modify my user info
			 */
			System.out.println("\n***********************************");
			System.out.println("Modify user " + myUser.getName() + " ==> ");
			System.out.println("***********************************");

			User modifiedUser = userEditor.modifyUser(myUser, null);

			System.out.println("Modified user: " + modifiedUser);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
