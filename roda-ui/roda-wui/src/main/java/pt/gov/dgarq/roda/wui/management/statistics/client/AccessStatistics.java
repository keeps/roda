package pt.gov.dgarq.roda.wui.management.statistics.client;

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
      wuiLogins = createStatisticPanel(constants.logWuiLoginTitle(), constants.logWuiLoginDesc(),
        "logs.action.RODAWUI.login", false, true, AGGREGATION_LAST);
      wuiPageHits = createStatisticPanel(constants.logWuiPageHitsTitle(), constants.logWuiPageHitsDesc(),
        "logs.action.RODAWUI.pageHit", false, true, AGGREGATION_LAST);
      wuiErrors = createStatisticPanel(constants.logWuiErrorsTitle(), constants.logWuiErrorsDesc(),
        "logs.action.RODAWUI.error", false, true, AGGREGATION_LAST);
      doViews = createStatisticPanel(constants.logDescriptiveMetadataViewsTitle(),
        constants.logDescriptiveMetadataViewsDesc(), "logs.action.Browser.getDescriptionObject", false, true,
        AGGREGATION_LAST);
      poViews = createStatisticPanel(constants.logPreservationMetadataViewsTitle(),
        constants.logPreservationMetadataViewsDesc(), "logs.action.Browser.getPreservationEvents", false, true,
        AGGREGATION_LAST);
      basicSearch = createStatisticPanel(constants.logBasicSearchTitle(), constants.logBasicSearchDesc(),
        "logs.action.Search.basicSearch", false, true, AGGREGATION_LAST);
      advancedSearch = createStatisticPanel(constants.logAdvancedSearchTitle(), constants.logAdvancedSearchDesc(),
        "logs.action.Search.advancedSearch", false, true, AGGREGATION_LAST);

      disseminatorHits = createStatisticPanel(constants.disseminatorHitsTitle(), constants.disseminatorHitsDesc(),
        "disseminator\\.hit\\..*", true, true, AGGREGATION_LAST);
      disseminatorMiss = createStatisticPanel(constants.disseminatorMissTitle(), constants.disseminatorMissDesc(),
        "disseminator\\.miss\\..*", true, true, AGGREGATION_LAST);

      registerUser = createStatisticPanel(constants.logRegisterUserTitle(), constants.logRegisterUserDesc(),
        "logs.action.UserRegistration.registerUser", false, true, AGGREGATION_LAST);

      userEmailConfirmation = createStatisticPanel(constants.logUserEmailConfirmationTitle(),
        constants.logUserEmailConfirmationDesc(), "logs.action.UserRegistration.confirmUserEmail", false, true,
        AGGREGATION_LAST);

      userPasswordReset = createStatisticPanel(constants.logUserPasswordResetTitle(),
        constants.logUserPasswordResetDesc(), "logs.action.UserRegistration.resetUserPassword", false, true,
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
    return constants.accessStatistics();
  }

}
