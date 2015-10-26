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
public class RepositoryStatistics extends StatisticTab {

  private VerticalPanel layout;
  private StatisticMiniPanel doCount;
  private StatisticMiniPanel fondsCount;
  private StatisticMiniPanel repCount;
  private StatisticMiniPanel repType;
  private StatisticMiniPanel repSubType;
  private StatisticMiniPanel sipCount;
  private StatisticMiniPanel presEventCount;
  private StatisticMiniPanel presRepCount;

  /**
   * Create new repository statistics
   */
  public RepositoryStatistics() {
    layout = new VerticalPanel();
    initWidget(layout);
  }

  @Override
  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      ret = true;
      doCount = createStatisticPanel(constants.descriptiveObjectsCountTitle(), constants.descriptiveObjectsCountDesc(),
        "object.descriptive", false, AGGREGATION_LAST);

      fondsCount = createStatisticPanel(constants.fondsCountTitle(), constants.fondsCountDesc(),
        "object.descriptive.fonds", false, AGGREGATION_LAST);

      repCount = createStatisticPanel(constants.representationObjectsCountTitle(),
        constants.representationObjectsCountDesc(), "object.representation", false, AGGREGATION_LAST);

      sipCount = createStatisticPanel(constants.sipCountTitle(), constants.sipCountDesc(), "sips", false,
        AGGREGATION_LAST);

      presEventCount = createStatisticPanel(constants.presEventCountTitle(), constants.presEventCountDesc(),
        "object.preservation.event", false, AGGREGATION_LAST);

      presRepCount = createStatisticPanel(constants.presRepCountTitle(), constants.presRepCountDesc(),
        "object.preservation.representation", false, AGGREGATION_LAST);

      repType = createStatisticPanel(constants.representationObjectTypeTitle(),
        constants.representationObjectTypeDesc(), "object\\.representation\\.type\\..*", true, AGGREGATION_LAST);

      repSubType = createStatisticPanel(constants.representationObjectSubTypeTitle(),
        constants.representationObjectSubTypeDesc(), "object\\.representation\\.subtype\\..*", true, AGGREGATION_LAST);

      layout.add(doCount);
      layout.add(fondsCount);
      layout.add(repCount);
      layout.add(sipCount);
      layout.add(presRepCount);
      layout.add(presEventCount);
      layout.add(repType);
      layout.add(repSubType);

      layout.addStyleName("wui-statistics-repository");

    }
    return ret;

  }

  @Override
  public String getTabText() {
    return constants.repositoryStatistics();
  }

}
