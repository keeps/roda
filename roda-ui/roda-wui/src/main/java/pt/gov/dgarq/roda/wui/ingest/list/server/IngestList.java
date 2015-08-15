package pt.gov.dgarq.roda.wui.ingest.list.server;

import java.util.Date;

import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class IngestList extends RodaCoreService {

	private static final String ROLE = "ingest.list_all_sips";

	private IngestList() {
		super();
	}

	public static Long countSipReports(RodaSimpleUser user, Filter filter)
			throws AuthorizationDeniedException, GenericException {
		Date start = new Date();

		// check user permissions
		UserUtility.checkRoles(user, ROLE);

		// delegate
		Long count = IngestListHelper.countSipReports(filter);

		// register action
		long duration = new Date().getTime() - start.getTime();
		registerAction(user, "IngestList", "countSipEntries", null, duration, "filter", filter.toString());

		return count;
	}

	public static IndexResult<SIPReport> findSipReports(RodaSimpleUser user, Filter filter, Sorter sorter,
			Sublist sublist, Facets facets) throws AuthorizationDeniedException, GenericException {
		Date start = new Date();

		// check user permissions
		UserUtility.checkRoles(user, ROLE);

		// delegate
		IndexResult<SIPReport> ret = IngestListHelper.findSipReports(filter, sorter, sublist, facets);

		// register action
		long duration = new Date().getTime() - start.getTime();
		registerAction(user, "IngestList", "findSipReports", null, duration, "filter", filter, "sorter", sorter,
				"sublist", sublist);

		return ret;
	}

	public static SIPReport retrieveSipReport(RodaSimpleUser user, String sipReportId)
			throws AuthorizationDeniedException, GenericException {
		Date start = new Date();

		// check user permissions
		UserUtility.checkRoles(user, ROLE);

		// delegate
		SIPReport ret = IngestListHelper.retrieveSipReport(sipReportId);

		// register action
		long duration = new Date().getTime() - start.getTime();
		registerAction(user, "IngestList", "getSipReport", null, duration, "sipReportId", sipReportId);

		return ret;
	}

}
