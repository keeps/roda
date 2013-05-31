/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

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

			createDescriptionObject = createStatisticPanel(constants
					.createDescriptionObjectTitle(), constants
					.createDescriptionObjectDesc(),
					"logs\\.user\\.Editor\\.createDescriptionObject\\..*",
					true, true, AGGREGATION_LAST);
			modifyDescriptionObject = createStatisticPanel(constants
					.modifyDescriptionObjectTitle(), constants
					.modifyDescriptionObjectDesc(),
					"logs\\.user\\.Editor\\.modifyDescriptionObject\\..*",
					true, true, AGGREGATION_LAST);
			removeDescriptionObject = createStatisticPanel(constants
					.removeDescriptionObjectTitle(), constants
					.removeDescriptionObjectDesc(),
					"logs\\.user\\.Editor\\.removeDescriptionObject\\..*",
					true, true, AGGREGATION_LAST);
			moveDescriptionObject = createStatisticPanel(constants
					.moveDescriptionObjectTitle(), constants
					.moveDescriptionObjectDesc(),
					"logs\\.user\\.Editor\\.moveDescriptionObject\\..*", true,
					true, AGGREGATION_LAST);
			addUser = createStatisticPanel(constants.addUserTitle(), constants
					.addUserDesc(),
					"logs\\.user\\.UserManagement\\.addUser\\..*", true, true,
					AGGREGATION_LAST);
			modifyUser = createStatisticPanel(constants.modifyUserTitle(),
					constants.modifyUserDesc(),
					"logs\\.user\\.UserManagement\\.modifyUser\\..*", true,
					true, AGGREGATION_LAST);
			removeUser = createStatisticPanel(constants.removeUserTitle(),
					constants.removeUserDesc(),
					"logs\\.user\\.UserManagement\\.removeUser\\..*", true,
					true, AGGREGATION_LAST);
			addGroup = createStatisticPanel(constants.addGroupTitle(),
					constants.addGroupDesc(),
					"logs\\.user\\.UserManagement\\.addGroup\\..*", true, true,
					AGGREGATION_LAST);
			modifyGroup = createStatisticPanel(constants.modifyGroupTitle(),
					constants.modifyGroupDesc(),
					"logs\\.user\\.UserManagement\\.modifyGroup\\..*", true,
					true, AGGREGATION_LAST);
			removeGroup = createStatisticPanel(constants.removeGroupTitle(),
					constants.removeGroupDesc(),
					"logs\\.user\\.UserManagement\\.removeGroup\\..*", true,
					true, AGGREGATION_LAST);

			setUserPassword = createStatisticPanel(constants
					.setUserPasswordTitle(), constants.setUserPasswordDesc(),
					"logs\\.user\\.UserManagement\\.setUserPassword\\..*",
					true, true, AGGREGATION_LAST);

			acceptSIP = createStatisticPanel(constants.acceptSIPTitle(),
					constants.acceptSIPDesc(),
					"logs\\.user\\.AcceptSIP\\.acceptSIP\\..*", true, true,
					AGGREGATION_LAST);

			addTask = createStatisticPanel(constants.addTaskTitle(), constants
					.addTaskDesc(), "logs\\.user\\.Scheduler\\.addTask\\..*",
					true, true, AGGREGATION_LAST);
			modifyTask = createStatisticPanel(constants.modifyTaskTitle(),
					constants.modifyTaskDesc(),
					"logs\\.user\\.Scheduler\\.modifyTask\\..*", true, true,
					AGGREGATION_LAST);
			removeTask = createStatisticPanel(constants.removeTaskTitle(),
					constants.removeTaskDesc(),
					"logs\\.user\\.Scheduler\\.removeTask\\..*", true, true,
					AGGREGATION_LAST);

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
	 * @see pt.gov.dgarq.roda.wui.management.statistics.client.StatisticTab#getTabText()
	 */
	@Override
	public String getTabText() {
		return constants.actionsStatistics();
	}

}
