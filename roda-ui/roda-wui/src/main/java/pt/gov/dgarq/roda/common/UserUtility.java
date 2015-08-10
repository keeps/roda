package pt.gov.dgarq.roda.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;

public class UserUtility {
	private static final Logger LOGGER = Logger.getLogger(UserUtility.class);
	private static final String RODA_CAS_USER = "CAS_USER";

	public static CASUserPrincipal getUser(HttpServletRequest request) {
		CASUserPrincipal cup = null;
		if(request.getSession().getAttribute(RODA_CAS_USER)!=null){
			cup = (CASUserPrincipal) request.getSession().getAttribute(RODA_CAS_USER);
		}
		return cup;
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

	public static void setUser(HttpServletRequest request,CASUserPrincipal casUserPrincipal) {
		LOGGER.error("SetUser()");
		request.getSession(true).setAttribute(RODA_CAS_USER, casUserPrincipal);
		
	}

	public static AuthenticatedUser loginCas(HttpServletRequest servletRequest, String url, String ticket, URL casURL, URL coreURL) throws MalformedURLException, AuthenticationException {
		CASUtility casUtility = new CASUtility(casURL, coreURL,new URL(url));
		CASUserPrincipal cup = casUtility.getCASUserPrincipal(null,
				ticket, servletRequest.getRemoteAddr());
		setUser(servletRequest, cup);
		return new AuthenticatedUser(cup, false);
	}
	
	public static void logout(HttpServletRequest servletRequest, URL casURL, URL coreURL){
		servletRequest.getSession().setAttribute(RODA_CAS_USER, null);
		servletRequest.getSession().removeAttribute("edu.yale.its.tp.cas.client.filter.user");
		servletRequest.getSession().removeAttribute("_const_cas_assertion_");
	}

	public static boolean haveSessionActive(CASUtility casUtility,HttpServletRequest servletRequest) {
		try{
			CASUserPrincipal cup = getUser(servletRequest);
			CASUserPrincipal cupUpdated = casUtility.getCASUserPrincipalFromProxyGrantingTicket(cup.getProxyGrantingTicket(), "");
			cupUpdated.setGuest(cup.isGuest());
			UserUtility.setUser(servletRequest, cupUpdated);
			return true;
		}catch(Exception e){
			return false;
		}
	}
}
