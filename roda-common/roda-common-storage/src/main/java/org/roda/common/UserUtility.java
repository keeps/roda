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
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class UserUtility {
  private static final Logger LOGGER = Logger.getLogger(UserUtility.class);
  private static final String RODA_USER = "RODA_USER";

  private static LdapUtility LDAP_UTILITY;
  private static RodaSimpleUser GUEST = null;

  /** Private empty constructor */
  private UserUtility() {

  }

  public static LdapUtility getLdapUtility() {
    return LDAP_UTILITY;
  }

  public static void setLdapUtility(LdapUtility ldapUtility) {
    LDAP_UTILITY = ldapUtility;
  }

  public static RodaUser getUser(HttpServletRequest request, IndexService indexService) {
    RodaUser user = null;
    if (request.getSession().getAttribute(RODA_USER) != null) {
      RodaSimpleUser rsu = (RodaSimpleUser) request.getSession().getAttribute(RODA_USER);

      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_ID, rsu.getId()));
      filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
      try {
        IndexResult<RODAMember> indexUsers = indexService.find(RODAMember.class, filter, null, new Sublist(), null);
        if (indexUsers.getTotalCount() == 1) {
          user = (RodaUser) indexUsers.getResults().get(0);
          user.setGuest(rsu.isGuest());
          LOGGER.trace("User obtained from index: " + user + "\n" + "user in session: " + rsu);
        } else {
          LOGGER.error("The number of users obtained from the index is different from 1");
        }
      } catch (IndexServiceException e) {
        LOGGER.error("Error obtaining user \"" + rsu.getId() + "\" from index", e);
      }
    } else {
      user = new RodaUser(getGuest());
    }
    return user;
  }

  public static void checkRoles(RodaUser rsu, List<String> rolesToCheck) throws AuthorizationDeniedException {
    if (!rsu.getAllRoles().containsAll(rolesToCheck)) {
      LOGGER.debug("User \"" + rsu.getId() + "\" roles: " + rsu.getAllRoles() + " vs. roles to check: " + rolesToCheck);
      throw new AuthorizationDeniedException(
        "The user '" + rsu.getId() + "' does not have all needed permissions: " + rolesToCheck);
    }
  }

  public static void checkRoles(RodaUser user, String... rolesToCheck) throws AuthorizationDeniedException {
    checkRoles(user, Arrays.asList(rolesToCheck));
  }

  public static RodaUser getFullUser(RodaSimpleUser rsu) throws LdapUtilityException {
    RodaUser u = UserUtility.getLdapUtility().getUser(rsu.getId());
    return u;
  }

  public static void setUser(HttpServletRequest request, RodaSimpleUser rsu) {
    request.getSession(true).setAttribute(RODA_USER, rsu);
  }

  public static void logout(HttpServletRequest servletRequest) {
    servletRequest.getSession().setAttribute(RODA_USER, getGuest());
    // CAS specific clean up
    servletRequest.getSession().removeAttribute("edu.yale.its.tp.cas.client.filter.user");
    servletRequest.getSession().removeAttribute("_const_cas_assertion_");
  }

  /**
   * Retrieves guest used. Note: this should be used as a read-only object
   */
  public static RodaSimpleUser getGuest() {
    if (GUEST == null) {
      GUEST = new RodaSimpleUser();
      GUEST.setId("guest");
      GUEST.setGuest(true);
    }
    return GUEST;
  }

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

  /**
   * @deprecated this method should not be used; it always returns
   *             {@code session.getId()}
   */
  @Deprecated
  public static String getClientUserPassword(HttpSession session) {
    return session.getId();
  }

  public static void checkObjectPermissions(RodaUser user, SimpleDescriptionObject sdo) {
    // FIXME implement

  }
}
