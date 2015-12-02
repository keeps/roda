/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.ingest.list.server;

import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.SIPReport;
import org.roda.wui.common.RodaCoreService;
import org.roda.wui.common.client.GenericException;

public class IngestList extends RodaCoreService {

  private static final String ROLE = "ingest.list_all_sips";

  private IngestList() {
    super();
  }

  public static Long countSipReports(RodaUser user, Filter filter)
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

  public static IndexResult<SIPReport> findSipReports(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ROLE);

    // delegate
    IndexResult<SIPReport> ret = IngestListHelper.findSipReports(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "IngestList", "findSipReports", null, duration, "filter", filter, "sorter", sorter, "sublist",
      sublist);

    return ret;
  }

  public static SIPReport retrieveSipReport(RodaUser user, String sipReportId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
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
