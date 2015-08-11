package pt.gov.dgarq.roda.wui.management.user.server;

import org.apache.log4j.Logger;
import org.roda.index.IndexServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
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

}
