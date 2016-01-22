/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.data.common.RODAException;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.common.client.ReportService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Report service implementation
 * 
 * @author Luis Faria
 * 
 */
public class ReportServiceImpl extends RemoteServiceServlet implements ReportService {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

  /**
   * Create a report service implementation
   */
  public ReportServiceImpl() {
  }

  public Report getReport(String reportId) throws RODAException {
    Report report = null;
    // try {
    // report = RodaClientFactory.getRodaClient(
    // getThreadLocalRequest().getSession()).getReportsService()
    // .getReport(reportId);
    // } catch (RemoteException e) {
    // logger.error("Error getting report " + reportId, e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return report;
  }
}
