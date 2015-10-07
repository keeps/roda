package org.roda.wui.common.server;

import org.apache.log4j.Logger;
import org.roda.core.common.RODAException;
import org.roda.core.data.Report;
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

  private static final Logger logger = Logger.getLogger(ReportServiceImpl.class);

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
