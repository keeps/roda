package org.roda.common;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;

import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.Pair;
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
  public static final String RODA_USER = "RODA_USER";

  private static LdapUtility LDAP_UTILITY;

  /** Private empty constructor */
  private UserUtility() {

  }

  public static LdapUtility getLdapUtility() {
    return LDAP_UTILITY;
  }

  public static void setLdapUtility(LdapUtility ldapUtility) {
    LDAP_UTILITY = ldapUtility;
  }

  // FIXME this method should be more auth scheme agnostic (basic auth vs. cas)
  public static RodaUser getApiUser(HttpServletRequest request, IndexService indexService)
    throws AuthorizationDeniedException {

    RodaUser user;
    Pair<String, String> credentials = getUserCredentialsFromBasicAuth(request);
    if (credentials != null) {
      try {
        user = UserUtility.getLdapUtility().getAuthenticatedUser(credentials.getFirst(), credentials.getSecond());
        user.setIpAddress(request.getRemoteAddr());
      } catch (AuthenticationDeniedException e) {
        throw new AuthorizationDeniedException("Unable to authenticate user!");
      } catch (ServiceException e) {
        throw new AuthorizationDeniedException("Unable to authenticate user!");
      }
    } else {
      user = getUser(request, indexService, false);
      if (user == null) {
        throw new AuthorizationDeniedException("No user provided!");
      }
      user.setIpAddress(request.getRemoteAddr());
    }
    return user;

  }

  private static Pair<String, String> getUserCredentialsFromBasicAuth(HttpServletRequest request) {
    Pair<String, String> ret = null;
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Basic")) {
      String credentials = new String(authorization);
      credentials = credentials.replaceFirst("[B|b]asic ", "");
      credentials = new String(Base64.getDecoder().decode(credentials), Charset.forName("UTF-8"));
      final String[] values = credentials.split(":", 2);
      if (values[0] != null && values[1] != null) {
        ret = new Pair<String, String>(values[0], values[1]);
      }
    }
    return ret;
  }

  public static RodaUser getUser(HttpServletRequest request, IndexService indexService,
    boolean returnGuestIfNoUserInSession) {
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
          user.setIpAddress(request.getRemoteAddr());
          LOGGER.trace("User obtained from index: " + user + "\n" + "user in session: " + rsu);
        } else {
          LOGGER.error("The number of users obtained from the index is different from 1");
        }
      } catch (IndexServiceException e) {
        LOGGER.error("Error obtaining user \"" + rsu.getId() + "\" from index", e);
      }
    } else {
      user = returnGuestIfNoUserInSession ? getGuest() : null;
    }
    return user;
  }

  public static RodaUser getUser(HttpServletRequest request, IndexService indexService) {
    return getUser(request, indexService, true);
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
   * Retrieves guest used
   */
  public static RodaUser getGuest() {
    RodaUser guest = new RodaUser();
    guest.setId("guest");
    guest.setName("guest");
    guest.setGuest(true);
    return guest;
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

  public static void checkObjectReadPermissions(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException {
    LOGGER.debug("Checking if user \"" + user.getId() + "\" has permissions to read object " + sdo.getId()
      + " (object read permissions: " + sdo.getPermissions().getReadUsers() + " & "
      + sdo.getPermissions().getReadGroups() + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!sdo.getPermissions().getReadUsers().contains(user.getId())
      && iterativeDisjoint(sdo.getPermissions().getReadGroups(), user.getAllGroups())) {
      throw new AuthorizationDeniedException("The user '" + user.getId() + "' does not have permissions to access!");
    }
  }

  public static void checkObjectGrantPermissions(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException {
    LOGGER.debug("Checking if user \"" + user.getId() + "\" has grant permissions to object " + sdo.getId()
      + " (object grant permissions: " + sdo.getPermissions().getGrantUsers() + " & "
      + sdo.getPermissions().getGrantGroups() + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!sdo.getPermissions().getGrantUsers().contains(user.getId())
      && iterativeDisjoint(sdo.getPermissions().getGrantGroups(), user.getAllGroups())) {
      throw new AuthorizationDeniedException("The user '" + user.getId() + "' does not have permissions to grant!");
    }
  }

  public static void checkObjectInsertPermissions(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException {
    LOGGER.debug("Checking if user \"" + user.getId() + "\" has insert permissions to object " + sdo.getId()
      + " (object insert permissions: " + sdo.getPermissions().getInsertUsers() + " & "
      + sdo.getPermissions().getInsertGroups() + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!sdo.getPermissions().getInsertUsers().contains(user.getId())
      && iterativeDisjoint(sdo.getPermissions().getInsertGroups(), user.getAllGroups())) {
      throw new AuthorizationDeniedException("The user '" + user.getId() + "' does not have permissions to insert!");
    }
  }

  public static void checkObjectModifyPermissions(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException {
    LOGGER.debug("Checking if user \"" + user.getId() + "\" has modify permissions to object " + sdo.getId()
      + " (object modify permissions: " + sdo.getPermissions().getModifyUsers() + " & "
      + sdo.getPermissions().getModifyGroups() + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!sdo.getPermissions().getModifyUsers().contains(user.getId())
      && iterativeDisjoint(sdo.getPermissions().getModifyGroups(), user.getAllGroups())) {
      throw new AuthorizationDeniedException("The user '" + user.getId() + "' does not have permissions to modify!");
    }
  }

  public static void checkObjectRemovePermissions(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException {
    LOGGER.debug("Checking if user \"" + user.getId() + "\" has remove permissions to object " + sdo.getId()
      + " (object modify permissions: " + sdo.getPermissions().getRemoveUsers() + " & "
      + sdo.getPermissions().getRemoveGroups() + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!sdo.getPermissions().getRemoveUsers().contains(user.getId())
      && iterativeDisjoint(sdo.getPermissions().getRemoveGroups(), user.getAllGroups())) {
      throw new AuthorizationDeniedException("The user '" + user.getId() + "' does not have permissions to remove!");
    }
  }

  private static boolean iterativeDisjoint(Set<String> set1, Set<String> set2) {
    boolean noCommonElement = true;
    for (String string : set1) {
      if (set2.contains(string)) {
        noCommonElement = false;
        break;
      }
    }
    return noCommonElement;
  }

}
