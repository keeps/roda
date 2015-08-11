package pt.gov.dgarq.roda.wui.management.user.server;

import java.util.Date;

import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class UserManagement extends RodaCoreService {

	private static final String ROLE = "administration.user";

	private UserManagement() {
		super();
	}

	public static Long countLogEntries(CASUserPrincipal user, Filter filter)
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

	public static IndexResult<LogEntry> findLogEntries(CASUserPrincipal user, Filter filter, Sorter sorter,
			Sublist sublist, Facets facets) throws AuthorizationDeniedException, GenericException {
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

}
