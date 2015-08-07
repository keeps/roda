package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.naming.AuthenticationException;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.servlet.LdapUtility;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * This class implements the Login service.
 * 
 * @author Rui Castro
 */
public class Login extends RODAWebService {

	static final private Logger logger = Logger.getLogger(Login.class);

	/**
	 * LDAP guest username (default: "guest")
	 */
	private String guestUsername = "guest";

	/**
	 * LDAP guest password (default: "guest")
	 */
	private String guestPassword = "guest";

	private CASUtility casUtility = null;

	/**
	 * Constructs a new Login service.
	 * 
	 * @throws RODAServiceException
	 */
	public Login() throws RODAServiceException {

		super();

		this.guestUsername = getConfiguration().getString("guestUsername",
				this.guestUsername);
		this.guestPassword = getConfiguration().getString("guestPassword",
				this.guestPassword);

		
		
		String casURL = getConfiguration().getString("roda.cas.url");
		String coreURL = getConfiguration().getString("roda.core.url");
		
		
		try {
			this.casUtility = new CASUtility(new URL(casURL),new URL(coreURL));
		}catch(MalformedURLException mfue){
			logger.error("Error initializing CASUtility:"+mfue.getMessage(),mfue);
		}


		logger.info(getClass().getSimpleName() + " initialised OK");
	}
	
	public User getAuthenticatedUserCAS(String proxyTicket)
			throws LoginException {
		try {

			Date start = new Date();
			// FIXME empty string
			CASUserPrincipal user = casUtility.getCASUserPrincipal(null,proxyTicket,"");
			
			long duration = new Date().getTime() - start.getTime();

			registerAction("Login.getAuthenticatedUser", new String[] {
					"PT", proxyTicket },
					"User %proxyTicket% called method Login.getAuthenticatedUser("
							+ proxyTicket + ")", duration);

			return (User)user;

		} catch (AuthenticationException e) {
			throw new LoginException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the user authenticated against stored credentials.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return an authenticated User.
	 * @throws LoginException
	 *             if the given credentials are not valid.
	 */
	/*
	public User getAuthenticatedUser(String username, String password)
			throws LoginException {

		try {

			Date start = new Date();
			User user = casUtility.getAuthenticatedUser(username, password);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Login.getAuthenticatedUser", new String[] {
					"username", username, "password", "***" },
					"User %username% called method Login.getAuthenticatedUser("
							+ username + ", " + "***" + ")", duration);

			return user;

		} catch (AuthenticationException e) {
			logger.debug(e.getMessage(), e);
			throw new LoginException(e.getMessage(), e);
		}
	}
	*/
	
	/**
	 * Gets the guest user authenticated against stored credentials.
	 * 
	 * @return an authenticated guest User.
	 * @throws LoginException
	 *             if the guest credentials are not valid.
	 */
	public User getGuestUser() throws LoginException {

		try {

			Date start = new Date();
			// FIXME empty string
			User user = casUtility.getCASUserPrincipal(guestUsername,
					guestPassword,"");
			long duration = new Date().getTime() - start.getTime();

			registerAction("Login.getGuestUser", new String[0],
					"User %username% called method Login.getGuestUser(" + ")",
					duration);

			return user;

		} catch (AuthenticationException e) {
			logger.debug(e.getMessage(), e);
			throw new LoginException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Returns the guest credentials (username, password).
	 * 
	 * @return an array with two values, the username and password of the guest
	 *         user.
	 */
	public String[] getGuestCredentials() {

		Date start = new Date();
		String[] guestCredentials = new String[] { guestUsername, guestPassword };
		long duration = new Date().getTime() - start.getTime();

		registerAction("Login.getGuestCredentials", new String[0],
				"User %username% called method Login.getGuestCredentials("
						+ ")", duration);

		return guestCredentials;
	}
}
