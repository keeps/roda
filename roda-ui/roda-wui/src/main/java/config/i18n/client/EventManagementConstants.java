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
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Luis Faria
 * 
 */
public interface EventManagementConstants extends Constants {

  /* Event Management */
  @DefaultStringValue("Scheduler")
  public String taskListTab();

  @DefaultStringValue("History")
  public String taskInstanceListTab();

  /* Task List */
  @DefaultStringValue("Name")
  public String taskHeaderName();

  @DefaultStringValue("Start date")
  public String taskHeaderStartDate();

  @DefaultStringValue("Repeat")
  public String taskHeaderRepeat();

  @DefaultStringValue("Username")
  public String taskHeaderUsername();

  /* Control Panel */
  @DefaultStringValue("List")
  public String controlPanelTitle();

  @DefaultStringValue("Name")
  public String controlPanelSearchTitle();

  @DefaultStringValue("scheduled")
  public String optionScheduled();

  @DefaultStringValue("not scheduled")
  public String optionNotScheduled();

  @DefaultStringValue("completed")
  public String optionNotRunning();

  @DefaultStringValue("all")
  public String optionAll();

  @DefaultStringValue("NEW")
  public String actionAddTask();

  @DefaultStringValue("EDIT")
  public String actionEditTask();

  @DefaultStringValue("REMOVE")
  public String actionRemoveTask();

  @DefaultStringValue("PAUSE")
  public String actionPauseTask();

  @DefaultStringValue("RESUME")
  public String actionResumeTask();

  /* Task Data */
  @DefaultStringValue("Name")
  public String taskName();

  @DefaultStringValue("Description")
  public String taskDescription();

  @DefaultStringValue("Start date")
  public String taskStartDate();

  @DefaultStringValue("Now")
  public String taskStartDateNow();

  @DefaultStringValue("Schedule")
  public String taskStartDateSchedule();

  @DefaultStringValue("Repeat")
  public String taskRepeat();

  @DefaultStringValue("no repeat")
  public String taskExecuteOnlyOnce();

  @DefaultStringValue("repeat")
  public String taskDoRepeat();

  @DefaultStringValue("each")
  public String taskRepeatIntervalLabel();

  @DefaultStringValue("seconds")
  public String taskIntervalMagnitudeSeconds();

  @DefaultStringValue("minutes")
  public String taskIntervalMagnitudeMinutes();

  @DefaultStringValue("hours")
  public String taskIntervalMagnitudeHours();

  @DefaultStringValue("days")
  public String taskIntervalMagnitudeDays();

  @DefaultStringValue("months")
  public String taskIntervalMagnitudeMonths();

  @DefaultStringValue("years")
  public String taskIntervalMagnitudeYears();

  @DefaultStringValue("until")
  public String taskRepeatUntil();

  @DefaultStringValue("forever")
  public String taskRepeatUntilForever();

  @DefaultStringValue("date")
  public String taskRepeatUntilDate();

  @DefaultStringValue("repetitions")
  public String taskRepeatUntilRepetitions();

  @DefaultStringValue("Plugin")
  public String taskPluginName();

  /* Plugin Panel */
  @DefaultStringValue("Parameters (*mandatory)")
  public String pluginParameters();

  /* Create Task */
  @DefaultStringValue("Schedule task")
  public String createTask();

  @DefaultStringValue("CANCEL")
  public String createTaskCancel();

  @DefaultStringValue("SAVE")
  public String createTaskCreate();

  @DefaultStringValue("Please complete all the required fields correctly")
  public String createTaskNotValid();

  /* Edit Task */
  @DefaultStringValue("edit task scheduler")
  public String editTask();

  @DefaultStringValue("CANCEL")
  public String editTaskCancel();

  @DefaultStringValue("SAVE")
  public String editTaskSave();

  @DefaultStringValue("Please complete all the required fields correctly")
  public String editTaskNotValid();

  /* Task Instance List */
  @DefaultStringValue("Name")
  public String instanceHeaderName();

  @DefaultStringValue("Start date")
  public String instanceHeaderStartDate();

  @DefaultStringValue("Complete")
  public String instanceHeaderCompleteness();

  @DefaultStringValue("User")
  public String instanceHeaderUser();

  @DefaultStringValue("CONTINUE")
  public String actionResumeInstance();

  @DefaultStringValue("PAUSE")
  public String actionPauseInstance();

  @DefaultStringValue("STOP")
  public String actionStopInstance();

  @DefaultStringValue("running")
  public String optionRunning();

  @DefaultStringValue("paused")
  public String optionPaused();

  @DefaultStringValue("finished")
  public String optionStopped();

}
