/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
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

  public static User getApiUser(final HttpServletRequest request) throws AuthorizationDeniedException {
    return getUser(request, false);
  }

  public static User getUser(final HttpServletRequest request, final boolean returnGuestIfNoUserInSession) {
    User user = (User) request.getSession().getAttribute(RODA_USER);
    if (user == null) {
      user = returnGuestIfNoUserInSession ? getGuest() : null;
    } else {
      if (user.isGuest()) {
        user = getGuest();
      }
    }
    return user;
  }

  public static User getUser(final HttpServletRequest request) {
    return getUser(request, true);
  }

  public static void checkRoles(final User rsu, final List<String> rolesToCheck) throws AuthorizationDeniedException {
    if (!rolesToCheck.isEmpty() && !rsu.getAllRoles().containsAll(rolesToCheck)) {
      final List<String> missingRoles = new ArrayList<String>(rolesToCheck);
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
      final List<String> roles = RodaCoreFactory.getRodaConfigurationAsList(configKey);
      checkRoles(user, roles);
    } else {
      LOGGER.error("Unable to determine which roles the user '{}' needs because the config. key '{}' is not defined",
        user.getName(), configKey);
      throw new AuthorizationDeniedException(
        "Unable to determine which roles the user needs because the config. key '" + configKey + "' is not defined");
    }
  }

  public static void setUser(final HttpServletRequest request, final User user) {
    request.getSession(true).setAttribute(RODA_USER, user);
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

  public static boolean isAdministrator(User user) {
    return user.getGroups().contains(RodaConstants.ADMINISTRATORS);
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
    }
  }

  private static <T extends IsIndexed> void checkObjectPermissions(User user, T obj, Function<T, String> toAIP,
    PermissionType permissionType) throws AuthorizationDeniedException {

    if (isAdministrator(user)) {
      return;
    }

    String aipId = toAIP.apply(obj);
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

  public static void checkRepresentationPermissions(User user, IndexedRepresentation rep, PermissionType permissionType)
    throws AuthorizationDeniedException {
    checkObjectPermissions(user, rep, r -> r.getAipId(), permissionType);
  }

  public static void checkFilePermissions(User user, IndexedFile file, PermissionType permissionType)
    throws AuthorizationDeniedException {
    checkObjectPermissions(user, file, f -> f.getAipId(), permissionType);
  }

  public static void checkPreservationEventPermissions(User user, IndexedPreservationEvent event,
    PermissionType permissionType) throws AuthorizationDeniedException {
    checkObjectPermissions(user, event, f -> f.getAipID(), permissionType);
  }

  public static void checkAIPPermissions(User user, SelectedItems<IndexedAIP> selected, PermissionType permission)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {

    if (isAdministrator(user)) {
      return;
    }

    IndexService index = RodaCoreFactory.getIndexService();
    if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedAIP> selectedItems = (SelectedItemsFilter<IndexedAIP>) selected;
      IterableIndexResult<IndexedAIP> findAll = index.findAll(IndexedAIP.class, selectedItems.getFilter(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);

      for (IndexedAIP aip : findAll) {
        checkAIPPermissions(user, aip, permission);
      }
    } else if (selected instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> selectedItems = (SelectedItemsList<IndexedAIP>) selected;
      List<IndexedAIP> aips = ModelUtils.getIndexedAIPsFromObjectIds(selectedItems);
      for (IndexedAIP aip : aips) {
        checkAIPPermissions(user, aip, permission);
      }
    } else {
      throw new RequestNotValidException(
        "SelectedItems implementations not supported: " + selected.getClass().getName());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> void checkObjectPermissions(User user, SelectedItems<T> selected,
    PermissionType permissionType) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Class<T> classToReturn = SelectedItemsUtils.parseClass(selected.getSelectedClass());

    if (classToReturn.equals(IndexedAIP.class)) {
      checkAIPPermissions(user, (SelectedItems<IndexedAIP>) selected, permissionType);
    } else if (classToReturn.equals(IndexedRepresentation.class)) {
      checkRepresentationPermissions(user, (SelectedItems<IndexedRepresentation>) selected, permissionType);
    } else if (classToReturn.equals(IndexedFile.class)) {
      checkFilePermissions(user, (SelectedItems<IndexedFile>) selected, permissionType);
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
      IterableIndexResult<T> findAll = index.findAll(classToReturn, selectedItems.getFilter(), fieldsToRequestIndex);

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
      user.setDirectRoles(new HashSet<String>(RodaUtils.copyList(defaultRoles)));
    } else {
      user.setDirectRoles(new HashSet<String>());
    }
    if (defaultGroups != null && !defaultGroups.isEmpty()) {
      user.setGroups(new HashSet<String>(RodaUtils.copyList(defaultGroups)));
    } else {
      user.setGroups(new HashSet<String>());
    }
    user.setActive(RodaCoreFactory.getRodaConfiguration().getBoolean(REGISTER_ACTIVE_PROPERTY));
    return user;
  }

}
