/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.statistics.client;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Luis Faria
 * 
 */
public class ActionsStatistics extends StatisticTab {

  private VerticalPanel layout;
  private StatisticMiniPanel createDescriptionObject;
  private StatisticMiniPanel modifyDescriptionObject;
  private StatisticMiniPanel removeDescriptionObject;
  private StatisticMiniPanel moveDescriptionObject;

  private StatisticMiniPanel addUser;
  private StatisticMiniPanel modifyUser;
  private StatisticMiniPanel removeUser;
  private StatisticMiniPanel addGroup;
  private StatisticMiniPanel modifyGroup;
  private StatisticMiniPanel removeGroup;
  private StatisticMiniPanel setUserPassword;

  private StatisticMiniPanel acceptSIP;

  private StatisticMiniPanel addTask;
  private StatisticMiniPanel modifyTask;
  private StatisticMiniPanel removeTask;

  /**
   * Create new actions statistics
   */
  public ActionsStatistics() {
    layout = new VerticalPanel();
    initWidget(layout);
  }

  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      ret = true;

      createDescriptionObject = createStatisticPanel(messages.createDescriptionObjectTitle(),
        messages.createDescriptionObjectDesc(), "logs\\.user\\.Editor\\.createDescriptionObject\\..*", true, true,
        AGGREGATION_LAST);
      modifyDescriptionObject = createStatisticPanel(messages.modifyDescriptionObjectTitle(),
        messages.modifyDescriptionObjectDesc(), "logs\\.user\\.Editor\\.modifyDescriptionObject\\..*", true, true,
        AGGREGATION_LAST);
      removeDescriptionObject = createStatisticPanel(messages.removeDescriptionObjectTitle(),
        messages.removeDescriptionObjectDesc(), "logs\\.user\\.Editor\\.removeDescriptionObject\\..*", true, true,
        AGGREGATION_LAST);
      moveDescriptionObject = createStatisticPanel(messages.moveDescriptionObjectTitle(),
        messages.moveDescriptionObjectDesc(), "logs\\.user\\.Editor\\.moveDescriptionObject\\..*", true, true,
        AGGREGATION_LAST);
      addUser = createStatisticPanel(messages.addUserTitle(), messages.addUserDesc(),
        "logs\\.user\\.UserManagement\\.addUser\\..*", true, true, AGGREGATION_LAST);
      modifyUser = createStatisticPanel(messages.modifyUserTitle(), messages.modifyUserDesc(),
        "logs\\.user\\.UserManagement\\.modifyUser\\..*", true, true, AGGREGATION_LAST);
      removeUser = createStatisticPanel(messages.removeUserTitle(), messages.removeUserDesc(),
        "logs\\.user\\.UserManagement\\.removeUser\\..*", true, true, AGGREGATION_LAST);
      addGroup = createStatisticPanel(messages.addGroupTitle(), messages.addGroupDesc(),
        "logs\\.user\\.UserManagement\\.addGroup\\..*", true, true, AGGREGATION_LAST);
      modifyGroup = createStatisticPanel(messages.modifyGroupTitle(), messages.modifyGroupDesc(),
        "logs\\.user\\.UserManagement\\.modifyGroup\\..*", true, true, AGGREGATION_LAST);
      removeGroup = createStatisticPanel(messages.removeGroupTitle(), messages.removeGroupDesc(),
        "logs\\.user\\.UserManagement\\.removeGroup\\..*", true, true, AGGREGATION_LAST);

      setUserPassword = createStatisticPanel(messages.setUserPasswordTitle(), messages.setUserPasswordDesc(),
        "logs\\.user\\.UserManagement\\.setUserPassword\\..*", true, true, AGGREGATION_LAST);

      acceptSIP = createStatisticPanel(messages.acceptSIPTitle(), messages.acceptSIPDesc(),
        "logs\\.user\\.AcceptSIP\\.acceptSIP\\..*", true, true, AGGREGATION_LAST);

      addTask = createStatisticPanel(messages.addTaskTitle(), messages.addTaskDesc(),
        "logs\\.user\\.Scheduler\\.addTask\\..*", true, true, AGGREGATION_LAST);
      modifyTask = createStatisticPanel(messages.modifyTaskTitle(), messages.modifyTaskDesc(),
        "logs\\.user\\.Scheduler\\.modifyTask\\..*", true, true, AGGREGATION_LAST);
      removeTask = createStatisticPanel(messages.removeTaskTitle(), messages.removeTaskDesc(),
        "logs\\.user\\.Scheduler\\.removeTask\\..*", true, true, AGGREGATION_LAST);

      // TODO add task instances actions

      layout.add(createDescriptionObject);
      layout.add(modifyDescriptionObject);
      layout.add(removeDescriptionObject);
      layout.add(moveDescriptionObject);
      layout.add(addUser);
      layout.add(addGroup);
      layout.add(modifyUser);
      layout.add(modifyGroup);
      layout.add(removeUser);
      layout.add(removeGroup);
      layout.add(setUserPassword);
      layout.add(acceptSIP);
      layout.add(addTask);
      layout.add(modifyTask);
      layout.add(removeTask);
    }

    return ret;
  }

  /**
   * @see org.roda.wui.management.statistics.client.StatisticTab#getTabText()
   */
  @Override
  public String getTabText() {
    return messages.actionsStatistics();
  }

}
