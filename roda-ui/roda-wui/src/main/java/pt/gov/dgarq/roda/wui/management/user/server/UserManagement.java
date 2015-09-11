package pt.gov.dgarq.roda.wui.management.user.server;

import java.util.Date;

import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class UserManagement extends RodaCoreService {

  private static final String ROLE = "administration.user";

  private UserManagement() {
    super();
  }

  public static Long countLogEntries(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException {
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
    Facets facets) throws AuthorizationDeniedException, GenericException {
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

  public static Long countMembers(RodaUser user, Filter filter) throws AuthorizationDeniedException, GenericException {
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
    Facets facets) throws AuthorizationDeniedException, GenericException {
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

  public static RodaGroup retrieveGroup(RodaUser user, String groupname)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    RodaGroup ret = UserManagementHelper.retrieveGroup(groupname);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "UserManagement", "retrieveGroup", null, duration, "groupname", groupname);

    return ret;
  }
}
