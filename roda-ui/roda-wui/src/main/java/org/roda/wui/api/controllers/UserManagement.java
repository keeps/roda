/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.Date;
import java.util.List;

import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.RodaCoreService;

public class UserManagement extends RodaCoreService {

  private static final String ROLE = "administration.user";

  private UserManagement() {
    super();
  }

  public static Long countLogEntries(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    Long count = UserManagementHelper.countLogEntries(filter);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "countLogEntries", null, duration, "filter", filter.toString());

    return count;
  }

  public static IndexResult<LogEntry> findLogEntries(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    IndexResult<LogEntry> ret = UserManagementHelper.findLogEntries(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "findLogEntries", null, duration, "filter", filter, "sorter", sorter,
      "sublist", sublist);

    return ret;
  }

  public static LogEntry retrieveLogEntry(RodaUser user, String logEntryId)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    Date start = new Date();
    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    LogEntry ret = UserManagementHelper.retrieveLogEntry(logEntryId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "retrieveLogEntry", null, duration, "logEntryId", logEntryId);

    return ret;
  }

  public static Long countMembers(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    Long count = UserManagementHelper.countMembers(filter);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "countMembers", null, duration, "filter", filter.toString());

    return count;
  }

  public static IndexResult<RODAMember> findMembers(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    IndexResult<RODAMember> ret = UserManagementHelper.findMembers(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "findMembers", null, duration, "filter", filter, "sorter", sorter, "sublist",
      sublist);

    return ret;
  }

  public static User retrieveUser(RodaUser user, String username)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    User ret = UserManagementHelper.retrieveUser(username);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "retrieveUser", null, duration, "username", username);

    return ret;
  }

  public static RodaUser retrieveRodaUser(RodaUser user, String username)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    RodaUser ret = UserManagementHelper.retrieveRodaUser(username);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "retrieveRodaUser", null, duration, "username", username);

    return ret;
  }

  public static Group retrieveGroup(RodaUser user, String groupname)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    Group ret = UserManagementHelper.retrieveGroup(groupname);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "retrieveGroup", null, duration, "groupname", groupname);

    return ret;
  }

  public static List<Group> listAllGroups(RodaUser user) throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    List<Group> ret = UserManagementHelper.listAllGroups();

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "listAllGroups", null, duration);

    return ret;
  }

  public static void register(User user, String password) throws EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, GenericException, NotFoundException {
    Date start = new Date();

    // delegate
    UserManagementHelper.addUser(user, password);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "register", null, duration, "username", user.getId());
  }

  public static void addUser(RodaUser user, User newUser, String password)
    throws AuthorizationDeniedException, NotFoundException, GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    UserManagementHelper.addUser(newUser, password);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "addUser", null, duration, "username", newUser.getId());
  }

  public static void modifyUser(RodaUser user, User modifiedUser, String password)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    UserManagementHelper.modifyUser(modifiedUser, password);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "modifyUser", null, duration, "username", modifiedUser.getId());
  }

  public static void removeUser(RodaUser user, String username) throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    UserManagementHelper.removeUser(username);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "removeUser", null, duration, "username", username);
  }

  public static void addGroup(RodaUser user, Group group)
    throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    UserManagementHelper.addGroup(group);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "addGroup", null, duration, "groupname", group.getId());
  }

  public static void modifyGroup(RodaUser user, Group group)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    UserManagementHelper.modifyGroup(group);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "modifyGroup", null, duration, "groupname", group.getId());
  }

  public static void removeGroup(RodaUser user, String groupname)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    UserManagementHelper.removeGroup(groupname);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "removeGroup", null, duration, "groupname", groupname);
  }
}
