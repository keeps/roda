/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.statistics.client;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Luis Faria
 * 
 */
public class AccessStatistics extends StatisticTab {

  private VerticalPanel layout;
  private StatisticMiniPanel wuiLogins;
  private StatisticMiniPanel wuiPageHits;
  private StatisticMiniPanel wuiErrors;
  private StatisticMiniPanel doViews;
  private StatisticMiniPanel poViews;
  private StatisticMiniPanel basicSearch;
  private StatisticMiniPanel advancedSearch;
  private StatisticMiniPanel registerUser;
  private StatisticMiniPanel userEmailConfirmation;
  private StatisticMiniPanel userPasswordReset;
  private StatisticMiniPanel disseminatorHits;
  private StatisticMiniPanel disseminatorMiss;

  /**
   * Create new access statistics
   */
  public AccessStatistics() {
    layout = new VerticalPanel();
    initWidget(layout);
  }

  @Override
  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      ret = true;
      wuiLogins = createStatisticPanel(messages.logWuiLoginTitle(), messages.logWuiLoginDesc(),
        "logs.action.RODAWUI.login", false, true, AGGREGATION_LAST);
      wuiPageHits = createStatisticPanel(messages.logWuiPageHitsTitle(), messages.logWuiPageHitsDesc(),
        "logs.action.RODAWUI.pageHit", false, true, AGGREGATION_LAST);
      wuiErrors = createStatisticPanel(messages.logWuiErrorsTitle(), messages.logWuiErrorsDesc(),
        "logs.action.RODAWUI.error", false, true, AGGREGATION_LAST);
      doViews = createStatisticPanel(messages.logDescriptiveMetadataViewsTitle(),
        messages.logDescriptiveMetadataViewsDesc(), "logs.action.Browser.getDescriptionObject", false, true,
        AGGREGATION_LAST);
      poViews = createStatisticPanel(messages.logPreservationMetadataViewsTitle(),
        messages.logPreservationMetadataViewsDesc(), "logs.action.Browser.getPreservationEvents", false, true,
        AGGREGATION_LAST);
      basicSearch = createStatisticPanel(messages.logBasicSearchTitle(), messages.logBasicSearchDesc(),
        "logs.action.Search.basicSearch", false, true, AGGREGATION_LAST);
      advancedSearch = createStatisticPanel(messages.logAdvancedSearchTitle(), messages.logAdvancedSearchDesc(),
        "logs.action.Search.advancedSearch", false, true, AGGREGATION_LAST);

      disseminatorHits = createStatisticPanel(messages.disseminatorHitsTitle(), messages.disseminatorHitsDesc(),
        "disseminator\\.hit\\..*", true, true, AGGREGATION_LAST);
      disseminatorMiss = createStatisticPanel(messages.disseminatorMissTitle(), messages.disseminatorMissDesc(),
        "disseminator\\.miss\\..*", true, true, AGGREGATION_LAST);

      registerUser = createStatisticPanel(messages.logRegisterUserTitle(), messages.logRegisterUserDesc(),
        "logs.action.UserRegistration.registerUser", false, true, AGGREGATION_LAST);

      userEmailConfirmation = createStatisticPanel(messages.logUserEmailConfirmationTitle(),
        messages.logUserEmailConfirmationDesc(), "logs.action.UserRegistration.confirmUserEmail", false, true,
        AGGREGATION_LAST);

      userPasswordReset = createStatisticPanel(messages.logUserPasswordResetTitle(),
        messages.logUserPasswordResetDesc(), "logs.action.UserRegistration.resetUserPassword", false, true,
        AGGREGATION_LAST);

      // TODO Add download of preservation metadata
      // TODO Add download of descriptive metadata

      layout.add(wuiLogins);
      layout.add(wuiPageHits);
      layout.add(wuiErrors);
      layout.add(doViews);
      layout.add(poViews);
      layout.add(basicSearch);
      layout.add(advancedSearch);
      layout.add(disseminatorHits);
      layout.add(disseminatorMiss);
      layout.add(registerUser);
      layout.add(userEmailConfirmation);
      layout.add(userPasswordReset);

    }
    return ret;

  }

  @Override
  public String getTabText() {
    return messages.accessStatistics();
  }

}
