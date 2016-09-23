/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
  public static final String RODA_USER = "RODA_USER";
  private static String REGISTER_ACTIVE_PROPERTY = "ui.register.active";
  private static String REGISTER_DEFAULT_GROUPS = "ui.register.defaultGroups";
  private static String REGISTER_DEFAULT_ROLES = "ui.register.defaultRoles";

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
  public static User getApiUser(HttpServletRequest request) throws AuthorizationDeniedException {

    User user;
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
      credentials = new String(Base64.getDecoder().decode(credentials),
        Charset.forName(RodaConstants.DEFAULT_ENCODING));
      final String[] values = credentials.split(":", 2);
      if (values[0] != null && values[1] != null) {
        ret = new Pair<String, String>(values[0], values[1]);
      }
    }
    return ret;
  }

  public static User getUser(HttpServletRequest request, boolean returnGuestIfNoUserInSession) {
    User user = null;
    if (request.getSession().getAttribute(RODA_USER) != null) {
      User rsu = (User) request.getSession().getAttribute(RODA_USER);
      if (!rsu.isGuest()) {
        try {
          user = UserUtility.getLdapUtility().getUser(rsu.getId());
        } catch (GenericException e) {
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

  public static User getUser(HttpServletRequest request) {
    return getUser(request, true);
  }

  public static void checkRoles(final User rsu, final List<String> rolesToCheck) throws AuthorizationDeniedException {
    if (!rolesToCheck.isEmpty()) {
      if (!rsu.getAllRoles().containsAll(rolesToCheck)) {
        LOGGER.debug("User '{}' roles: {} vs. roles to check: {}", rsu.getId(), rsu.getAllRoles(), rolesToCheck);
        throw new AuthorizationDeniedException(
          "The user '" + rsu.getId() + "' does not have all needed permissions: " + rolesToCheck);
      }
    }
  }

  public static void checkGroup(final User rsu, final String group) throws AuthorizationDeniedException {
    if (!rsu.getGroups().contains(group)) {
      LOGGER.debug("User '{}' groups: {} vs. group to check: {}", rsu.getId(), rsu.getGroups(), group);
      throw new AuthorizationDeniedException(
        "The user '" + rsu.getId() + "' does not belong to the group '" + group + "'");
    }
  }

  public static void checkRoles(final User user, final String... rolesToCheck) throws AuthorizationDeniedException {
    checkRoles(user, Arrays.asList(rolesToCheck));
  }

  public static void checkRoles(final User user, final Class<?> invokingMethodInnerClass)
    throws AuthorizationDeniedException {
    checkRoles(user, invokingMethodInnerClass, null);
  }

  public static void checkRoles(final User user, final Class<?> invokingMethodInnerClass, final Class<?> classToReturn)
    throws AuthorizationDeniedException {
    final Method method = invokingMethodInnerClass.getEnclosingMethod();
    final String classParam = (classToReturn == null) ? "" : "(" + classToReturn.getSimpleName() + ")";
    final String configKey = String.format("core.roles.%s.%s%s", method.getDeclaringClass().getName(), method.getName(),
      classParam);
    if (RodaCoreFactory.getRodaConfiguration().containsKey(configKey)) {
      final List<String> roles = RodaCoreFactory.getRodaConfigurationAsList(configKey);
      checkRoles(user, roles);
    } else {
      LOGGER.error("Unable to determine which roles the user '{}' needs because the config. key '{}' is not defined",
        user.getName(), configKey);
      throw new AuthorizationDeniedException(
        "Unable to determine which roles the user needs because the config. key '" + configKey + "' is not defined");
    }
  }

  public static void setUser(HttpServletRequest request, User rsu) {
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
  public static User getGuest() {
    return new User("guest", "guest", true);
  }

  public static void checkObjectPermissions(User user, IndexedAIP aip, PermissionType permissionType)
    throws AuthorizationDeniedException {

    Set<String> users = aip.getPermissions().getUsers().get(permissionType);
    Set<String> groups = aip.getPermissions().getGroups().get(permissionType);

    LOGGER.debug("Checking if user '{}' has permissions to {} object {} (object read permissions: {} & {})",
      user.getId(), permissionType, aip.getId(), users, groups);

    // FIXME
    if ("admin".equalsIgnoreCase(user.getId())) {
      return;
    }

    if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getGroups())) {
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

  /**
   * This method make sure that a normal user can only upload a file to a folder
   * with its own username
   *
   * @param user
   * @param ids
   * @throws AuthorizationDeniedException
   */
  public static void checkTransferredResourceAccess(User user, List<String> ids) throws AuthorizationDeniedException {
    // FIXME provide a better method for ensuring that producers don't change
    // others files
    // if ("admin".equalsIgnoreCase(user.getId())) {
    // return;
    // } else {
    // for (String id : ids) {
    //
    // if (id == null && !"admin".equals(user.getName())) {
    // throw new AuthorizationDeniedException(
    // "The user '" + user.getId() + "' does not have permissions to create
    // resource in root!");
    // } else {
    // try {
    // IndexService index = RodaCoreFactory.getIndexService();
    // TransferredResource parent = index.retrieve(TransferredResource.class,
    // id);
    // if
    // (!Paths.get(parent.getRelativePath()).getName(0).toString().equalsIgnoreCase(user.getName()))
    // {
    // throw new AuthorizationDeniedException("The user '" + user.getId()
    // + "' does not have permissions to access to transferred resource " + id +
    // " !");
    // }
    // } catch (GenericException | NotFoundException e) {
    // throw new AuthorizationDeniedException("The user '" + user.getId()
    // + "' does not have permissions to access to transferred resource " + id +
    // " !");
    // }
    //
    // }
    // }
    // }
  }

  public static void checkObjectPermissions(User user, SelectedItems<IndexedAIP> selected, PermissionType permission)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
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

  public static User resetGroupsAndRoles(User user) {
    List<Object> defaultRoles = RodaCoreFactory.getRodaConfiguration().getList(REGISTER_DEFAULT_ROLES);
    List<Object> defaultGroups = RodaCoreFactory.getRodaConfiguration().getList(REGISTER_DEFAULT_GROUPS);

    if (defaultRoles != null && defaultRoles.size() > 0) {
      user.setDirectRoles(new HashSet<String>(RodaUtils.copyList(defaultRoles)));
    } else {
      user.setDirectRoles(new HashSet<String>());
    }
    if (defaultGroups != null && defaultGroups.size() > 0) {
      user.setGroups(new HashSet<String>(RodaUtils.copyList(defaultGroups)));
    } else {
      user.setGroups(new HashSet<String>());
    }
    user.setActive(RodaCoreFactory.getRodaConfiguration().getBoolean(REGISTER_ACTIVE_PROPERTY));
    return user;
  }

}
