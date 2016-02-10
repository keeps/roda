/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.LdapUtilityException;
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
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagementHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementHelper.class);

  protected static Long countLogEntries(Filter filter) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(LogEntry.class, filter);
  }

  protected static IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(LogEntry.class, filter, sorter, sublist, facets);
  }

  public static LogEntry retrieveLogEntry(String logEntryId) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(LogEntry.class, logEntryId);
  }

  protected static Long countMembers(Filter filter) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(RODAMember.class, filter);
  }

  protected static IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(RODAMember.class, filter, sorter, sublist, facets);
  }

  protected static RodaUser retrieveRodaUser(String username) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(RodaUser.class, username);
  }

  protected static User retrieveUser(String username) throws GenericException {
    try {
      return UserUtility.getLdapUtility().getUser(username);
    } catch (LdapUtilityException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
  }

  protected static Group retrieveGroup(String groupname)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    try {
      return UserUtility.getLdapUtility().getGroup(groupname);
    } catch (LdapUtilityException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
  }

  protected static List<Group> listAllGroups() throws GenericException {
    try {
      return UserUtility.getLdapUtility().getGroups(null);
    } catch (LdapUtilityException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
  }

  public static void registerUser(User user, String password)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    RodaCoreFactory.getModelService().registerUser(user, password, true, true);
  }

  public static void addUser(User user, String password) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    RodaCoreFactory.getModelService().addUser(user, password, true, true);
  }

  public static void modifyUser(User user, String password)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().modifyUser(user, password, true, true);
  }

  public static void removeUser(String username) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().removeUser(username, true, true);
  }

  public static void addGroup(Group group) throws GenericException, AlreadyExistsException {
    RodaCoreFactory.getModelService().addGroup(group, true, true);
  }

  public static void modifyGroup(Group group) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().modifyGroup(group, true, true);
  }

  public static void removeGroup(String groupname) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().removeGroup(groupname, true, true);
  }

  public static User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().confirmUserEmail(username, email, emailConfirmationToken, true, true);
  }

  public static User requestPasswordReset(String username, String email)
    throws IllegalOperationException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().requestPasswordReset(username, email, true, true);
  }

  public static User resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().resetUserPassword(username, password, resetPasswordToken, true, true);
  }
}
