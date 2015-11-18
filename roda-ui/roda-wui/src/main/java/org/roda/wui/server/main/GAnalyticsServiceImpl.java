/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.RodaCoreFactory;
import org.roda.wui.client.main.GAnalyticsService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service for Google Analytics.
 */
public class GAnalyticsServiceImpl extends RemoteServiceServlet implements GAnalyticsService {

  private static final long serialVersionUID = -3032914449117878609L;
  private static final Logger logger = LoggerFactory.getLogger(GAnalyticsServiceImpl.class);
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
