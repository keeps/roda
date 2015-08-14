package pt.gov.dgarq.roda.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.directory.server.core.api.DirectoryService;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;

public class UserUtility {
	private static final Logger LOGGER = Logger.getLogger(UserUtility.class);
	private static final String RODA_USER = "RODA_USER";
	
	
	
	private static DirectoryService DIRECTORY_SERVICE;

	public static DirectoryService getDirectoryService() {
		return DIRECTORY_SERVICE;
	}

	public static void setDirectoryService(DirectoryService ds) {
		DIRECTORY_SERVICE = ds;
	}
	

	public static RodaSimpleUser getUser(HttpServletRequest request) {
		RodaSimpleUser rsu = null;
		if(request.getSession().getAttribute(RODA_USER)!=null){
			rsu = (RodaSimpleUser) request.getSession().getAttribute(RODA_USER);
		}
		return rsu;
	}

	public static void checkRoles(RodaSimpleUser rsu, List<String> rolesToCheck)
			throws AuthorizationDeniedException {
		RodaUser ru = getFullUser(rsu);
		if (!Arrays.asList(ru.getRoles()).containsAll(rolesToCheck)) {
			throw new AuthorizationDeniedException(
					"The user '" + ru.getUsername() + "' does not have all needed permissions: " + rolesToCheck);
		}
	}
	
	
	public static void checkRoles(RodaSimpleUser rsu, List<String> rolesToCheck, HttpServletRequest request)
			throws AuthorizationDeniedException {
		RodaUser ru = getFullUser(rsu);
		if (!Arrays.asList(ru.getRoles()).containsAll(rolesToCheck)) {
			throw new AuthorizationDeniedException(
					"The user '" + ru.getUsername() + "' does not have all needed permissions: " + rolesToCheck);
		}
	}

	public static RodaUser getFullUser(RodaSimpleUser rsu) {
		RodaUser u = new RodaUser(rsu);
		Set<String> roles = new HashSet<>();
		roles.add("browse");
		roles.add("search");
		roles.add("administration.user");
		roles.add("administration.event");
		roles.add("administration.metadata_editor");
		roles.add("administration.statistics");
		roles.add("administration.statistics.monitor");
		roles.add("logger.monitor");
		roles.add("ingest.pre_ingest");
		roles.add("ingest.load_sips");
		roles.add("ingest.list_my_sips");
		roles.add("ingest.list_all_sips");
		roles.add("ingest.accept_reject_sip");
		roles.add("misc.logger");
		roles.add("misc.register_user");
		roles.add("misc.browse_users");

		u.setRoles(roles);
		u.setDirectRoles(roles);
		return u;
	}

	public static void checkRoles(RodaSimpleUser user, HttpServletRequest request, String... rolesToCheck) throws AuthorizationDeniedException {
		checkRoles(user, Arrays.asList(rolesToCheck),request);
	}
	public static void checkRoles(RodaSimpleUser user, String... rolesToCheck) throws AuthorizationDeniedException {
		checkRoles(user, Arrays.asList(rolesToCheck),null);
	}

	public static void setUser(HttpServletRequest request,RodaSimpleUser rsu) {
		request.getSession(true).setAttribute(RODA_USER, rsu);
		
	}
	public static void logout(HttpServletRequest servletRequest){
		servletRequest.getSession().setAttribute(RODA_USER, null);
		servletRequest.getSession().removeAttribute("edu.yale.its.tp.cas.client.filter.user");
		servletRequest.getSession().removeAttribute("_const_cas_assertion_");
	}
/*
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
	}*/
}
