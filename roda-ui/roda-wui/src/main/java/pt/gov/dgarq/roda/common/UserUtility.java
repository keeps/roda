package pt.gov.dgarq.roda.common;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

public class UserUtility {
	private static final Logger LOGGER = Logger.getLogger(UserUtility.class);

	public static CASUserPrincipal getUser(HttpServletRequest request) {
		CASUserPrincipal user = null;

		if (request != null) {
			if (request instanceof RodaServletRequestWrapper) {
				RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) request;
				user = rodaRequestWrapper.getCASUserPrincipal();
			} else {
				LOGGER.error("Error getting user");

			}
		}

		return user;
	}

	public static void checkRoles(CASUserPrincipal user, List<String> rolesToCheck)
			throws AuthorizationDeniedException {
		if (!Arrays.asList(user.getRoles()).containsAll(rolesToCheck)) {
			throw new AuthorizationDeniedException(
					"The user '" + user.getName() + "' does not have all needed permissions: " + rolesToCheck);
		}
	}

	public static void checkRoles(CASUserPrincipal user, String... rolesToCheck) throws AuthorizationDeniedException {
		checkRoles(user, Arrays.asList(rolesToCheck));
	}
}
