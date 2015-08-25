package org.roda.common;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;

public class UserUtility {
	private static final Logger LOGGER = Logger.getLogger(UserUtility.class);
	private static final String RODA_USER = "RODA_USER";

	private static LdapUtility LDAP_UTILITY;

	public static LdapUtility getLdapUtility() {
		return LDAP_UTILITY;
	}

	public static void setLdapUtility(LdapUtility ldapUtility) {
		LDAP_UTILITY = ldapUtility;
	}

	public static RodaUser getUser(HttpServletRequest request, IndexService indexService) {
		RodaUser user = null;
		RodaSimpleUser rsu = null;
		if (request.getSession().getAttribute(RODA_USER) != null) {
			rsu = (RodaSimpleUser) request.getSession().getAttribute(RODA_USER);

			Filter filter = new Filter();
			filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_ID, rsu.getId()));
			filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
			try {
				IndexResult<RODAMember> indexUsers = indexService.find(RODAMember.class, filter, null, new Sublist(),
						null);
				if (indexUsers.getTotalCount() == 1) {
					user = (RodaUser) indexUsers.getResults().get(0);
					user.setGuest(rsu.isGuest());
					LOGGER.debug("User obtained from index: " + user + "\n" + "user in session: " + rsu);
				} else {
					LOGGER.debug("List of users found: " + indexUsers.getResults());
					LOGGER.error("The number of users obtained from the index is different from 1");
				}
			} catch (IndexServiceException e) {
				LOGGER.error("Error obtaining user \"" + rsu.getId() + "\" from index", e);
			}
		}
		return user;
	}

	public static void checkRoles(RodaUser rsu, List<String> rolesToCheck)
			throws AuthorizationDeniedException, LdapUtilityException {
		if (!Arrays.asList(rsu.getAllRoles()).containsAll(rolesToCheck)) {
			throw new AuthorizationDeniedException(
					"The user '" + rsu.getId() + "' does not have all needed permissions: " + rolesToCheck);
		}
	}

	public static void checkRoles(RodaUser ru, List<String> rolesToCheck, HttpServletRequest request)
			throws AuthorizationDeniedException {
		if (!Arrays.asList(ru.getAllRoles()).containsAll(rolesToCheck)) {
			throw new AuthorizationDeniedException(
					"The user '" + ru.getId() + "' does not have all needed permissions: " + rolesToCheck);
		}
	}

	public static RodaUser getFullUser(RodaSimpleUser rsu) throws LdapUtilityException {
		RodaUser u = UserUtility.getLdapUtility().getUser(rsu.getId());

		LOGGER.debug("Getting user " + rsu);

		u.setAllGroups(u.getAllGroups());
		u.setDirectGroups(u.getDirectGroups());
		u.setAllRoles(u.getAllRoles());
		u.setDirectRoles(u.getDirectRoles());
		return u;
	}

	public static void checkRoles(RodaUser user, HttpServletRequest request, String... rolesToCheck)
			throws AuthorizationDeniedException {
		checkRoles(user, Arrays.asList(rolesToCheck), request);
	}

	public static void checkRoles(RodaUser user, String... rolesToCheck) throws AuthorizationDeniedException {
		checkRoles(user, Arrays.asList(rolesToCheck), null);
	}

	public static void setUser(HttpServletRequest request, RodaSimpleUser rsu) {
		request.getSession(true).setAttribute(RODA_USER, rsu);
	}

	public static void logout(HttpServletRequest servletRequest) {
		servletRequest.getSession().setAttribute(RODA_USER, null);
		servletRequest.getSession().removeAttribute("edu.yale.its.tp.cas.client.filter.user");
		servletRequest.getSession().removeAttribute("_const_cas_assertion_");
	}
	/*
	 * public static boolean haveSessionActive(CASUtility
	 * casUtility,HttpServletRequest servletRequest) { try{ CASUserPrincipal cup
	 * = getUser(servletRequest); CASUserPrincipal cupUpdated =
	 * casUtility.getCASUserPrincipalFromProxyGrantingTicket(cup.
	 * getProxyGrantingTicket(), ""); cupUpdated.setGuest(cup.isGuest());
	 * UserUtility.setUser(servletRequest, cupUpdated); return true;
	 * }catch(Exception e){ return false;Set<String> roles = new
	 * HashSet<String>(); roles.add("browse"); roles.add("search");
	 * roles.add("administration.user"); roles.add("administration.event");
	 * roles.add("administration.metadata_editor");
	 * roles.add("administration.statistics");
	 * roles.add("administration.statistics.monitor");
	 * roles.add("logger.monitor"); roles.add("ingest.pre_ingest");
	 * roles.add("ingest.load_sips"); roles.add("ingest.list_my_sips");
	 * roles.add("ingest.list_all_sips"); roles.add("ingest.accept_reject_sip");
	 * roles.add("misc.logger"); roles.add("misc.register_user");
	 * roles.add("misc.browse_users");
	 * 
	 * u.setAllRoles(roles); u.setDirectRoles(roles); } }
	 */

	public static RodaSimpleUser getClientUser(HttpSession session) {
		final RodaSimpleUser rsu;
		if (session.getAttribute(RODA_USER) != null) {
			rsu = (RodaSimpleUser) session.getAttribute(RODA_USER);
		} else {
			rsu = null;
		}
		return rsu;
	}

	public static String getClientUserName(HttpSession session) {
		final String username;
		if (session.getAttribute(RODA_USER) != null) {
			username = ((RodaSimpleUser) session.getAttribute(RODA_USER)).getName();
		} else {
			username = null;
		}
		return username;

	}

	// TODO ????
	public static String getClientUserPassword(HttpSession session) {
		return null;
	}
}
