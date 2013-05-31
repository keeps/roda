package pt.gov.dgarq.roda.core.services;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserManagementException;

/**
 * @author Rui Castro
 * 
 */
public class UserPasswordTest {

	static final private Logger logger = Logger
			.getLogger(UserPasswordTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			UserManagement userManagement = new UserManagement();

			userManagement.setUserPassword("rcastro", "rcastro");

		} catch (IllegalOperationException e) {
			logger.error("IllegalOperationException", e);
		} catch (NoSuchUserException e) {
			logger.error("NoSuchUserException", e);
		} catch (UserManagementException e) {
			logger.error("UserManagementException", e);
		} catch (RODAServiceException e) {
			logger.error("RODAServiceException", e);
		}
	}
}
