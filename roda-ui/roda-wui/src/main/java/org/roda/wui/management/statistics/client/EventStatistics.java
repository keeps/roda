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
public class EventStatistics extends StatisticTab {

  private VerticalPanel layout;
  private StatisticMiniPanel taskCount;
  private StatisticMiniPanel taskState;
  private StatisticMiniPanel instanceCount;
  private StatisticMiniPanel instanceState;

  /**
   * Create new repository statistics
   */
  public EventStatistics() {
    layout = new VerticalPanel();
    initWidget(layout);
  }

  @Override
  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      ret = true;
      taskCount = createStatisticPanel(messages.taskCountTitle(), messages.taskCountDesc(), "tasks", false,
        AGGREGATION_LAST);

      taskState = createStatisticPanel(messages.taskStateTitle(), messages.taskStateDesc(), "tasks\\.state\\..*", true,
        AGGREGATION_LAST);

      instanceCount = createStatisticPanel(messages.taskInstanceCountTitle(), messages.taskInstanceCountDesc(),
        "instances", false, AGGREGATION_LAST);

      instanceState = createStatisticPanel(messages.taskInstanceStateTitle(), messages.taskInstanceStateDesc(),
        "instances\\.state\\..*", true, AGGREGATION_LAST);

      layout.add(taskCount);
      layout.add(taskState);
      layout.add(instanceCount);
      layout.add(instanceState);

      layout.addStyleName("wui-statistics-events");

    }
    return ret;

  }

  @Override
  public String getTabText() {
    return messages.repositoryStatistics();
  }

}
