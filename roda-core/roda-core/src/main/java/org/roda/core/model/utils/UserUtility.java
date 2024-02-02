/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.IndexUtils;
import org.roda.core.index.utils.IterableIndexResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import jakarta.servlet.http.HttpServletRequest;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
  private static final String RODA_USER = "RODA_USER";
  private static final String REGISTER_ACTIVE_PROPERTY = "ui.register.active";
  private static final String REGISTER_DEFAULT_GROUPS = "ui.register.defaultGroups";
  private static final String REGISTER_DEFAULT_ROLES = "ui.register.defaultRoles";

  private static LdapUtility ldapUtility;

  /** Private empty constructor */
  private UserUtility() {
    // do nothing
  }

  public static LdapUtility getLdapUtility() {
    return ldapUtility;
  }

  public static void setLdapUtility(LdapUtility utility) {
    ldapUtility = utility;
  }

  public static boolean isUserInSession(final HttpServletRequest request) {
    User user = (User) request.getSession().getAttribute(UserUtility.RODA_USER);
    return user != null && !user.isGuest();
  }

  public static User getApiUser(final HttpServletRequest request) {
    return getUser(request, false);
  }

  public static User getUser(final HttpServletRequest request, final boolean returnGuestIfNoUserInSession) {
    User user = (User) request.getSession().getAttribute(RODA_USER);
    if (user == null) {
      if (returnGuestIfNoUserInSession) {
        user = getGuest(request.getRemoteAddr());
        request.getSession().setAttribute(RODA_USER, user);
      }
    } else {
      if (user.isGuest()) {
        user = getGuest(request.getRemoteAddr());
      }
    }
    return user;
  }

  public static User getUser(final HttpServletRequest request) {
    return getUser(request, true);
  }

  public static void setUser(final HttpServletRequest request, final User user) {
    user.setIpAddress(request.getRemoteAddr());
    request.getSession(true).setAttribute(RODA_USER, user);
  }

  public static void removeUserFromSession(final HttpServletRequest request,
    List<String> extraAttributesToBeRemovedFromSession) {
    // internal session clean up
    request.getSession().removeAttribute(RODA_USER);

    for (String attribute : extraAttributesToBeRemovedFromSession) {
      request.getSession().removeAttribute(attribute);
    }
  }

  public static void checkRoles(final User rsu, final List<String> rolesToCheck) throws AuthorizationDeniedException {
    // INFO 20170220 nvieira containsAll changed to set intersection (contain at
    // least one role)
    if (!rolesToCheck.isEmpty() && Sets.intersection(rsu.getAllRoles(), new HashSet<>(rolesToCheck)).isEmpty()) {
      final List<String> missingRoles = new ArrayList<>(rolesToCheck);
      missingRoles.removeAll(rsu.getAllRoles());

      throw new AuthorizationDeniedException("The user '" + rsu.getId() + "' does not have all needed permissions",
        missingRoles);
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
      LOGGER.trace("Testing if user '{}' has permissions to '{}'", user.getName(), configKey);
      final List<String> roles = RodaCoreFactory.getRodaConfigurationAsList(configKey);
      checkRoles(user, roles);
    } else {
      LOGGER.error("Unable to determine which roles the user '{}' needs because the config. key '{}' is not defined",
        user.getName(), configKey);
      throw new AuthorizationDeniedException(
        "Unable to determine which roles the user needs because the config. key '" + configKey + "' is not defined");
    }
  }

  public static <T extends IsIndexed> void checkObjectPermissions(final User user, T obj,
    final Class<?> invokingMethodInnerClass, final Class<?> classToReturn) throws AuthorizationDeniedException {
    if (SolrCollection.hasPermissionFilters(obj.getClass())) {
      final Method method = invokingMethodInnerClass.getEnclosingMethod();
      final String classParam = (classToReturn == null) ? "" : "(" + classToReturn.getSimpleName() + ")";
      final String configKey = String.format("core.permissions.%s.%s%s", method.getDeclaringClass().getName(),
        method.getName(), classParam);

      if (RodaCoreFactory.getRodaConfiguration().containsKey(configKey)) {
        LOGGER.trace("Testing if user '{}' has permissions to '{}'", user.getName(), configKey);
        String configValue = RodaCoreFactory.getRodaConfigurationAsString(configKey);

        try {
          PermissionType permissionType = PermissionType.valueOf(configValue);
          checkObjectPermissions(user, obj, permissionType);
        } catch (IllegalArgumentException e) {
          LOGGER.error(
            "Unable to determine which permissions the user '{}' needs because the config value '{}' is not a permission type",
            user.getName(), configValue);
          throw new AuthorizationDeniedException(
            "Unable to determine which permissions the user needs because the config value '" + configValue
              + "' is not a permission type");
        }
      } else {
        LOGGER.error(
          "Unable to determine which permissions the user '{}' needs because the config key '{}' is not defined",
          user.getName(), configKey);
        throw new AuthorizationDeniedException(
          "Unable to determine which permissions the user needs because the config key '" + configKey
            + "' is not defined");
      }
    }
  }

  public static <T extends IsIndexed> void checkObjectPermissions(final User user, SelectedItems<T> objs,
    final Class<?> invokingMethodInnerClass, final Class<?> classToReturn)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    if (RodaConstants.WHITELIST_CLASS_NAMES.contains(objs.getSelectedClass())) {
      try {
        if (SolrCollection.hasPermissionFilters(Class.forName(objs.getSelectedClass()))) {
          final Method method = invokingMethodInnerClass.getEnclosingMethod();
          final String classParam = (classToReturn == null) ? "" : "(" + classToReturn.getSimpleName() + ")";
          final String configKey = String.format("core.permissions.%s.%s%s", method.getDeclaringClass().getName(),
            method.getName(), classParam);
          if (RodaCoreFactory.getRodaConfiguration().containsKey(configKey)) {
            LOGGER.trace("Testing if user '{}' has permissions to '{}'", user.getName(), configKey);
            String configValue = RodaCoreFactory.getRodaConfigurationAsString(configKey);

            try {
              PermissionType permissionType = PermissionType.valueOf(configValue);
              checkObjectPermissions(user, objs, permissionType);
            } catch (IllegalArgumentException e) {
              LOGGER.error(
                "Unable to determine which permissions the user '{}' needs because the config value '{}' is not a permission type",
                user.getName(), configValue);
              throw new AuthorizationDeniedException(
                "Unable to determine which permissions the user needs because the config value '" + configValue
                  + "' is not a permission type");
            }
          } else {
            LOGGER.error(
              "Unable to determine which permissions the user '{}' needs because the config. key '{}' is not defined",
              user.getName(), configKey);
            throw new AuthorizationDeniedException(
              "Unable to determine which permissions the user needs because the config. key '" + configKey
                + "' is not defined");
          }
        }
      } catch (ClassNotFoundException e) {
        throw new GenericException(e);
      }
    } else {
      throw new GenericException("Invalid value for class name " + objs.getSelectedClass());
    }
  }

  public static void checkUserApiBasicAuth(String username) throws AuthenticationDeniedException {
    if (RodaCoreFactory.getRodaConfiguration().getBoolean(RodaConstants.CORE_API_BASIC_AUTH_DISABLE, false)) {
      List<String> allowedUsers = RodaCoreFactory
              .getRodaConfigurationAsList(RodaConstants.CORE_API_BASIC_AUTH_WHITELIST);
      if (allowedUsers.isEmpty() || !allowedUsers.contains(username)) {
        throw new AuthenticationDeniedException("User is not authorized to use API");
      }
    }
  }

  /**
   * Retrieves guest used
   */
  public static User getGuest(String ipAddress) {
    User guest = null;
    try {
      guest = ldapUtility.getUser("guest").setIpAddress(ipAddress);
    } catch (GenericException e) {
      LOGGER.warn("Could not get user 'guest' from ldap", e);
    }

    if (guest == null) {
      guest = new User("guest", "guest", true).setIpAddress(ipAddress);
    }

    guest.setGuest(true);
    return guest;
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
   */
  public static void checkTransferredResourceAccess(User user, List<String> ids) {
    // do nothing
  }

  public static boolean isAdministrator(String username) {
    return username.equals(RodaConstants.ADMIN);
  }

  public static boolean isAdministrator(User user) {
    return user.getName().equals(RodaConstants.ADMIN);
  }

  public static void checkAIPPermissions(User user, IndexedAIP aip, PermissionType permissionType)
    throws AuthorizationDeniedException {

    if (isAdministrator(user)) {
      return;
    }

    Set<String> users = aip.getPermissions().getUsers().get(permissionType);
    Set<String> groups = aip.getPermissions().getGroups().get(permissionType);

    LOGGER.debug("Checking if user '{}' has permissions to {} object {} (object read permissions: {} & {})",
      user.getId(), permissionType, aip.getId(), users, groups);

    if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getGroups())) {
      throw new AuthorizationDeniedException(
        "The user '" + user.getId() + "' does not have permissions to " + permissionType);
    }
  }

  public static void checkDIPPermissions(User user, IndexedDIP dip, PermissionType permissionType)
    throws AuthorizationDeniedException {

    if (isAdministrator(user)) {
      return;
    }

    Set<String> users = dip.getPermissions().getUsers().get(permissionType);
    Set<String> groups = dip.getPermissions().getGroups().get(permissionType);

    LOGGER.debug("Checking if user '{}' has permissions to {} dip {} (object read permissions: {} & {})", user.getId(),
      permissionType, dip.getId(), users, groups);

    if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getGroups())) {
      throw new AuthorizationDeniedException(
        "The user '" + user.getId() + "' does not have permissions to " + permissionType);
    }
  }

  public static <T extends IsIndexed> void checkObjectPermissions(User user, T obj, PermissionType permissionType)
    throws AuthorizationDeniedException {
    if (obj instanceof IndexedAIP) {
      checkAIPPermissions(user, (IndexedAIP) obj, permissionType);
    } else if (obj instanceof IndexedRepresentation) {
      checkRepresentationPermissions(user, (IndexedRepresentation) obj, permissionType);
    } else if (obj instanceof IndexedFile) {
      checkFilePermissions(user, (IndexedFile) obj, permissionType);
    } else if (obj instanceof IndexedDIP) {
      checkDIPPermissions(user, (IndexedDIP) obj, permissionType);
    } else if (obj instanceof DIPFile) {
      checkDIPFilePermissions(user, (DIPFile) obj, permissionType);
    } else if (obj instanceof IndexedPreservationEvent) {
      checkPreservationEventPermissions(user, (IndexedPreservationEvent) obj, permissionType);
    }
  }

  private static <T extends IsIndexed> void checkAIPObjectPermissions(User user, T obj, Function<T, String> toAIP,
    PermissionType permissionType) throws AuthorizationDeniedException {

    if (isAdministrator(user)) {
      return;
    }

    String aipId = toAIP.apply(obj);
    if (aipId != null) {
      IndexedAIP aip;
      try {
        aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      } catch (NotFoundException | GenericException e) {
        throw new AuthorizationDeniedException("Could not check permissions of object " + obj, e);
      }

      Set<String> users = aip.getPermissions().getUsers().get(permissionType);
      Set<String> groups = aip.getPermissions().getGroups().get(permissionType);

      LOGGER.debug("Checking if user '{}' has permissions to {} object {} (object read permissions: {} & {})",
        user.getId(), permissionType, aip.getId(), users, groups);

      if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getGroups())) {
        throw new AuthorizationDeniedException(
          "The user '" + user.getId() + "' does not have permissions to " + permissionType);
      }
    }
  }

  private static <T extends IsIndexed> void checkDIPObjectPermissions(User user, T obj, Function<T, String> toDIP,
    PermissionType permissionType) throws AuthorizationDeniedException {

    if (isAdministrator(user)) {
      return;
    }

    String dipId = toDIP.apply(obj);
    IndexedDIP dip;
    try {
      dip = RodaCoreFactory.getIndexService().retrieve(IndexedDIP.class, dipId,
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
    } catch (NotFoundException | GenericException e) {
      throw new AuthorizationDeniedException("Could not check permissions of object " + obj, e);
    }

    Set<String> users = dip.getPermissions().getUsers().get(permissionType);
    Set<String> groups = dip.getPermissions().getGroups().get(permissionType);

    LOGGER.debug("Checking if user '{}' has permissions to {} object {} (object read permissions: {} & {})",
      user.getId(), permissionType, dip.getId(), users, groups);

    if (!users.contains(user.getId()) && iterativeDisjoint(groups, user.getGroups())) {
      throw new AuthorizationDeniedException(
        "The user '" + user.getId() + "' does not have permissions to " + permissionType);
    }
  }

  public static void checkRepresentationPermissions(User user, IndexedRepresentation rep, PermissionType permissionType)
    throws AuthorizationDeniedException {
    checkAIPObjectPermissions(user, rep, r -> r.getAipId(), permissionType);
  }

  public static void checkFilePermissions(User user, IndexedFile file, PermissionType permissionType)
    throws AuthorizationDeniedException {
    checkAIPObjectPermissions(user, file, f -> f.getAipId(), permissionType);
  }

  public static void checkDIPFilePermissions(User user, DIPFile file, PermissionType permissionType)
    throws AuthorizationDeniedException {
    checkDIPObjectPermissions(user, file, f -> f.getDipId(), permissionType);
  }

  public static void checkPreservationEventPermissions(User user, IndexedPreservationEvent event,
    PermissionType permissionType) throws AuthorizationDeniedException {
    checkAIPObjectPermissions(user, event, f -> f.getAipID(), permissionType);
  }

  public static void checkAIPPermissions(User user, SelectedItems<IndexedAIP> selected, PermissionType permission)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {

    if (isAdministrator(user)) {
      return;
    }

    IndexService index = RodaCoreFactory.getIndexService();
    if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedAIP> selectedItems = (SelectedItemsFilter<IndexedAIP>) selected;
      try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, selectedItems.getFilter(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN)) {

        for (IndexedAIP aip : result) {
          checkAIPPermissions(user, aip, permission);
        }
      } catch (IOException e) {
        LOGGER.error("Error getting AIPs to check permissions", e);
      }
    } else if (selected instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> selectedItems = (SelectedItemsList<IndexedAIP>) selected;
      List<IndexedAIP> aips = IndexUtils.getIndexedAIPsFromObjectIds(selectedItems);
      for (IndexedAIP aip : aips) {
        checkAIPPermissions(user, aip, permission);
      }
    } else {
      throw new RequestNotValidException(
        "SelectedItems implementations not supported: " + selected.getClass().getName());
    }
  }

  public static void checkDIPPermissions(User user, SelectedItems<IndexedDIP> selected, PermissionType permission)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {

    if (isAdministrator(user)) {
      return;
    }

    IndexService index = RodaCoreFactory.getIndexService();
    if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedDIP> selectedItems = (SelectedItemsFilter<IndexedDIP>) selected;
      try (IterableIndexResult<IndexedDIP> findAll = index.findAll(IndexedDIP.class, selectedItems.getFilter(),
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN)) {

        for (IndexedDIP dip : findAll) {
          checkDIPPermissions(user, dip, permission);
        }
      } catch (IOException e) {
        LOGGER.error("Error getting DIPs to check permissions", e);
      }
    } else if (selected instanceof SelectedItemsList) {
      SelectedItemsList<IndexedDIP> selectedItems = (SelectedItemsList<IndexedDIP>) selected;
      List<IndexedDIP> dips = IndexUtils.getIndexedDIPsFromObjectIds(selectedItems);
      for (IndexedDIP dip : dips) {
        checkDIPPermissions(user, dip, permission);
      }
    } else {
      throw new RequestNotValidException(
        "SelectedItems implementations not supported: " + selected.getClass().getName());
    }
  }

  public static <T extends IsIndexed> void checkObjectPermissions(User user, SelectedItems<T> selected,
    PermissionType permissionType) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Class<T> classToReturn = SelectedItemsUtils.parseClass(selected.getSelectedClass());

    if (classToReturn.equals(IndexedAIP.class)) {
      checkAIPPermissions(user, (SelectedItems<IndexedAIP>) selected, permissionType);
    } else if (classToReturn.equals(IndexedRepresentation.class)) {
      checkRepresentationPermissions(user, (SelectedItems<IndexedRepresentation>) selected, permissionType);
    } else if (classToReturn.equals(IndexedFile.class)) {
      checkFilePermissions(user, (SelectedItems<IndexedFile>) selected, permissionType);
    } else if (classToReturn.equals(IndexedDIP.class)) {
      checkDIPPermissions(user, (SelectedItems<IndexedDIP>) selected, permissionType);
    }
  }

  private static <T extends IsIndexed> void checkObjectPermissions(User user, SelectedItems<T> selected,
    Function<T, String> toAIP, PermissionType permission, List<String> fieldsToRequestIndex)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {

    if (isAdministrator(user)) {
      return;
    }

    Class<T> classToReturn = SelectedItemsUtils.parseClass(selected.getSelectedClass());
    IndexService index = RodaCoreFactory.getIndexService();
    if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<T> selectedItems = (SelectedItemsFilter<T>) selected;
      try (IterableIndexResult<T> findAll = index.findAll(classToReturn, selectedItems.getFilter(),
        fieldsToRequestIndex)) {

        for (T obj : findAll) {
          String aipId = toAIP.apply(obj);
          IndexedAIP aip;
          try {
            aip = index.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
            checkAIPPermissions(user, aip, permission);
          } catch (NotFoundException e) {
            // conservative approach
            throw new AuthorizationDeniedException(
              "Could not verify permissions of object [" + classToReturn.getSimpleName() + "] " + obj.getUUID(), e);
          }
        }
      } catch (IOException e) {
        LOGGER.error("Error getting objects to check permissions", e);
      }
    } else if (selected instanceof SelectedItemsList) {
      SelectedItemsList<T> selectedItems = (SelectedItemsList<T>) selected;

      List<IndexedAIP> aips = new ArrayList<>();
      for (String uuid : selectedItems.getIds()) {
        T obj;
        try {
          obj = index.retrieve(classToReturn, uuid, fieldsToRequestIndex);
          String aipId = toAIP.apply(obj);
          IndexedAIP aip = index.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
          aips.add(aip);
        } catch (NotFoundException e) {
          // conservative approach
          throw new AuthorizationDeniedException(
            "Could not verify permissions of object [" + classToReturn.getSimpleName() + "] " + uuid, e);
        }

      }

      for (IndexedAIP aip : aips) {
        checkAIPPermissions(user, aip, permission);
      }
    } else {
      throw new RequestNotValidException(
        "SelectedItems implementations not supported: " + selected.getClass().getName());
    }
  }

  public static void checkRepresentationPermissions(User user, SelectedItems<IndexedRepresentation> selected,
    PermissionType permission) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    checkObjectPermissions(user, selected, rep -> rep.getAipId(), permission,
      RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
  }

  public static void checkFilePermissions(User user, SelectedItems<IndexedFile> selected, PermissionType permission)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    checkObjectPermissions(user, selected, file -> file.getAipId(), permission, RodaConstants.FILE_FIELDS_TO_RETURN);
  }

  public static User resetGroupsAndRoles(User user) {
    List<Object> defaultRoles = RodaCoreFactory.getRodaConfiguration().getList(REGISTER_DEFAULT_ROLES);
    List<Object> defaultGroups = RodaCoreFactory.getRodaConfiguration().getList(REGISTER_DEFAULT_GROUPS);

    if (defaultRoles != null && !defaultRoles.isEmpty()) {
      user.setDirectRoles(new HashSet<>(RodaUtils.copyList(defaultRoles)));
    } else {
      user.setDirectRoles(new HashSet<>());
    }
    if (defaultGroups != null && !defaultGroups.isEmpty()) {
      user.setGroups(new HashSet<>(RodaUtils.copyList(defaultGroups)));
    } else {
      user.setGroups(new HashSet<>());
    }
    user.setActive(RodaCoreFactory.getRodaConfiguration().getBoolean(REGISTER_ACTIVE_PROPERTY));
    return user;
  }

  public static boolean hasPermissions(User user, String... methods) {
    return hasPermissions(user, Arrays.asList(methods), null);
  }

  public static boolean hasPermissions(User user, Permissions permissions, String... methods) {
    return hasPermissions(user, Arrays.asList(methods), permissions);
  }

  public static boolean hasPermissions(User user, List<String> methods, Permissions permissions) {
    boolean canAct = true;
    for (String method : methods) {
      canAct &= user.hasRole(RodaCoreFactory.getRodaConfigurationAsString("core.roles." + method));

      String permissionKey = RodaCoreFactory.getRodaConfigurationAsString("core.permissions." + method);
      if (canAct && permissions != null && permissionKey != null) {
        PermissionType permissionType = PermissionType.valueOf(permissionKey);

        if (permissionType != null) {
          if (permissions.getUserPermissions(user.getName()).contains(permissionType)) {
            canAct = true;
          } else {
            boolean containGroup = false;
            for (String group : user.getGroups()) {
              if (permissions.getGroupPermissions(group).contains(permissionType)) {
                containGroup = true;
                break;
              }
            }

            canAct = containGroup;
          }
        }
      }
    }

    return canAct;
  }
}
