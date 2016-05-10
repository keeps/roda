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

import org.roda.core.RodaCoreFactory;
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
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
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

  // FIXME 20151002 hsilva: this method should be more auth scheme agnostic
  // (basic auth vs. cas)
  public static RodaUser getApiUser(HttpServletRequest request) throws AuthorizationDeniedException {

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
      user = getUser(request, false);
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

  public static RodaUser getUser(HttpServletRequest request, boolean returnGuestIfNoUserInSession) {
    RodaUser user = null;
    if (request.getSession().getAttribute(RODA_USER) != null) {
      RodaSimpleUser rsu = (RodaSimpleUser) request.getSession().getAttribute(RODA_USER);
      if (!rsu.isGuest()) {
        try {
          user = UserUtility.getLdapUtility().getUser(rsu.getId());
        } catch (LdapUtilityException e) {
          LOGGER.error("Could not login", e);
        }
      } else {
        user = getGuest();
      }
    } else {
      user = returnGuestIfNoUserInSession ? getGuest() : null;
    }
    return user;
  }

  public static RodaUser getUser(HttpServletRequest request) {
    return getUser(request, true);
  }

  public static void checkRoles(RodaUser rsu, List<String> rolesToCheck) throws AuthorizationDeniedException {
    if (!rsu.getAllRoles().containsAll(rolesToCheck)) {
      LOGGER.debug("User \"{}\" roles: {} vs. roles to check: {}", rsu.getId(), rsu.getAllRoles(), rolesToCheck);
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

    LOGGER.debug("Checking if user \"{}\" has permissions to {} object {} (object read permissions: {} & {})",
      user.getId(), permissionType, aip.getId(), users, groups);

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

  public static void checkObjectPermissions(RodaUser user, SelectedItems<IndexedAIP> selected,
    PermissionType permission) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    IndexService index = RodaCoreFactory.getIndexService();
    if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedAIP> selectedItems = (SelectedItemsFilter<IndexedAIP>) selected;

      long count = index.count(IndexedAIP.class, selectedItems.getFilter());
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItems.getFilter(), null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
        for (IndexedAIP aip : aips) {
          checkObjectPermissions(user, aip, permission);
        }
      }
    } else {
      SelectedItemsList<IndexedAIP> selectedItems = (SelectedItemsList<IndexedAIP>) selected;
      List<IndexedAIP> aips = ModelUtils.getIndexedAIPsFromObjectIds(selectedItems);
      for (IndexedAIP aip : aips) {
        checkObjectPermissions(user, aip, permission);
      }
    }

  }
}
