package pt.gov.dgarq.roda.wui.ingest.list.server;

import org.apache.log4j.Logger;
import org.roda.index.IndexServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class IngestListHelper {
	private static final Logger LOGGER = Logger.getLogger(IngestListHelper.class);

	static Long countSipReports(Filter filter) throws GenericException {
		Long count;
		try {
			count = RodaCoreFactory.getIndexService().count(SIPReport.class, filter);
		} catch (IndexServiceException e) {
			LOGGER.debug("Error getting SIP reports count", e);
			throw new GenericException("Error getting SIP reports count " + e.getMessage());
		}

		return count;
	}

	static IndexResult<SIPReport> findSipReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
			throws GenericException {
		IndexResult<SIPReport> ret;
		try {
			ret = RodaCoreFactory.getIndexService().find(SIPReport.class, filter, sorter, sublist, facets);
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting SIP reports", e);
			throw new GenericException("Error getting SIP reports " + e.getMessage());
		}

		return ret;
	}

	static SIPReport retrieveSipReport(String sipReportId) throws GenericException {
		SIPReport ret;
		try {
			ret = RodaCoreFactory.getIndexService().retrieve(SIPReport.class, sipReportId);
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting SIP reports", e);
			throw new GenericException("Error getting SIP reports " + e.getMessage());
		}

		return ret;
	}
}
