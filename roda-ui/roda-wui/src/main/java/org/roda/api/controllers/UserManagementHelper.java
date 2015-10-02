package org.roda.api.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.roda.common.LdapUtilityException;
import org.roda.common.UserUtility;
import org.roda.index.IndexServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class UserManagementHelper {
  private static final Logger LOGGER = Logger.getLogger(UserManagementHelper.class);

  protected static Long countLogEntries(Filter filter) throws GenericException {
    Long count;
    try {
      count = RodaCoreFactory.getIndexService().count(LogEntry.class, filter);
    } catch (IndexServiceException e) {
      LOGGER.debug("Error getting log entries count", e);
      throw new GenericException("Error getting log entries count " + e.getMessage());
    }

    return count;
  }

  protected static IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException {
    IndexResult<LogEntry> ret;
    try {
      ret = RodaCoreFactory.getIndexService().find(LogEntry.class, filter, sorter, sublist, facets);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting log entries", e);
      throw new GenericException("Error getting log entries " + e.getMessage());
    }

    return ret;

  }

  public static LogEntry retrieveLogEntry(String logEntryId) throws GenericException {
    LogEntry ret;
    try {
      ret = RodaCoreFactory.getIndexService().retrieve(LogEntry.class, logEntryId);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting log entries", e);
      throw new GenericException("Error getting log entry " + e.getMessage());
    }

    return ret;
  }

  protected static Long countMembers(Filter filter) throws GenericException {
    Long count;
    try {
      count = RodaCoreFactory.getIndexService().count(RODAMember.class, filter);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting member count", e);
      throw new GenericException("Error getting log entries: " + e.getMessage());
    }
    return count;
  }

  protected static IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException {
    IndexResult<RODAMember> ret;
    try {
      ret = RodaCoreFactory.getIndexService().find(RODAMember.class, filter, sorter, sublist, facets);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting member list", e);
      throw new GenericException("Error getting member list: " + e.getMessage());
    }
    return ret;
  }

  protected static RodaUser retrieveRodaUser(String username) throws GenericException {
    RodaUser ret;
    try {
      ret = RodaCoreFactory.getIndexService().retrieve(RodaUser.class, username);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
    return ret;
  }

  protected static User retrieveUser(String username) throws GenericException {
    try {
      return UserUtility.getLdapUtility().getUser(username);
    } catch (LdapUtilityException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
  }

  protected static Group retrieveGroup(String groupname) throws AuthorizationDeniedException, GenericException {
    Group ret;
    try {
      ret = RodaCoreFactory.getIndexService().retrieve(Group.class, groupname);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting group", e);
      throw new GenericException("Error getting group: " + e.getMessage());
    }
    return ret;
  }

  protected static List<Group> listAllGroups() throws GenericException {
    try {
      return UserUtility.getLdapUtility().getGroups(null);
    } catch (LdapUtilityException e) {
      LOGGER.error("Error getting user", e);
      throw new GenericException("Error getting user: " + e.getMessage());
    }
  }

}
