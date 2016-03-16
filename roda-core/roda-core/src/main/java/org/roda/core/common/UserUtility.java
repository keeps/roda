/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.index.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
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
      } catch (AuthenticationDeniedException | ServiceException e) {
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
      } catch (GenericException | RequestNotValidException e) {
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

  public static void checkObjectPermissions(RodaUser user, IndexedAIP aip, PermissionType permissionType)
    throws AuthorizationDeniedException {

    Set<String> users = aip.getPermissions().getUsers().get(permissionType);
    Set<String> groups = aip.getPermissions().getGroups().get(permissionType);

    LOGGER.debug("Checking if user \"" + user.getId() + "\" has permissions to " + permissionType + " object "
      + aip.getId() + " (object read permissions: " + users + " & " + groups + ")");

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getAllGroups())) {
      throw new AuthorizationDeniedException(
        "The user '" + user.getId() + "' does not have permissions to " + permissionType);
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

  public static void checkTransferredResourceAccess(RodaUser user, List<String> ids)
    throws AuthorizationDeniedException {
    // FIXME administrator workaround
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    } else {
      for (String id : ids) {
        if (id == null && !"admin".equals(user.getName())) {
          throw new AuthorizationDeniedException(
            "The user '" + user.getId() + "' does not have permissions to create resource in root!");
        } else if (!Paths.get(id).getName(0).toString().equalsIgnoreCase(user.getName())) {
          throw new AuthorizationDeniedException(
            "The user '" + user.getId() + "' does not have permissions to access to transferred resource " + id + " !");
        }
      }
    }
  }

  public static void checkRoles(Function<?, ?> function) {
    // TODO Auto-generated method stub

  }

}
