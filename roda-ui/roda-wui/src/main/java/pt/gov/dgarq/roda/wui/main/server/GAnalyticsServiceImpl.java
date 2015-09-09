package pt.gov.dgarq.roda.wui.main.server;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.wui.main.client.GAnalyticsService;

/**
 * The server side implementation of the RPC service for Google Analytics.
 */
public class GAnalyticsServiceImpl extends RemoteServiceServlet implements GAnalyticsService {

  private static final long serialVersionUID = -3032914449117878609L;
  private static final Logger logger = Logger.getLogger(GAnalyticsServiceImpl.class);
  private static String GANALYTICS_ACCOUNT_CODE = null;

  public GAnalyticsServiceImpl() {
    // do nothing
  }

  @Override
  public String getGoogleAnalyticsAccount() {
    if (GANALYTICS_ACCOUNT_CODE == null) {
      GANALYTICS_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString("roda.wui.ga.code", "");
      logger.debug("Google Analytics Account Code: " + GANALYTICS_ACCOUNT_CODE);
    }
    return GANALYTICS_ACCOUNT_CODE;
  }

}
