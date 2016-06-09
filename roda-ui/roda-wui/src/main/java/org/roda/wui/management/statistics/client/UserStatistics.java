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
public class UserStatistics extends StatisticTab {

  private VerticalPanel layout;
  private StatisticMiniPanel userCount;
  private StatisticMiniPanel groupCount;
  private StatisticMiniPanel groupTop5;

  /**
   * Create new user statistics
   */
  public UserStatistics() {
    layout = new VerticalPanel();
    initWidget(layout);
  }

  @Override
  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      ret = true;
      userCount = createStatisticPanel(messages.userCountTitle(), messages.userCountDesc(), "users\\.state\\..*", true,
        AGGREGATION_LAST);
      groupCount = createStatisticPanel(messages.groupCountTitle(), messages.groupCountDesc(), "groups", false,
        AGGREGATION_LAST);

      groupTop5 = createStatisticPanel(messages.groupTop5Title(), messages.groupTop5Desc(), "users\\.group\\..*", true,
        AGGREGATION_LAST);

      layout.add(userCount);
      layout.add(groupCount);
      layout.add(groupTop5);

      layout.addStyleName("wui-statistics-user");

    }
    return ret;

  }

  @Override
  public String getTabText() {
    return messages.userStatistics();
  }

}
