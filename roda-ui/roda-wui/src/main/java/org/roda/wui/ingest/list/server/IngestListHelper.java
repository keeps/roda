/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.ingest.list.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SIPReport;
import org.roda.core.index.IndexServiceException;
import org.roda.wui.common.client.GenericException;

public class IngestListHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestListHelper.class);

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
