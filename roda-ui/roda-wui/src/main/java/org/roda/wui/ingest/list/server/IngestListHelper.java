/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.ingest.list.server;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SIPReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestListHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestListHelper.class);

  static Long countSipReports(Filter filter) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(SIPReport.class, filter);
  }

  static IndexResult<SIPReport> findSipReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(SIPReport.class, filter, sorter, sublist, facets);
  }

  static SIPReport retrieveSipReport(String sipReportId) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(SIPReport.class, sipReportId);
  }
}
