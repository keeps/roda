/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 * 
 */
public interface EventManagementMessages extends Messages {

  /* Task List */
  @DefaultMessage("{0} scheduled tasks")
  public String taskListTotal(Integer taskCount);

  /* Task Instance List */
  @DefaultMessage("{0} tasks")
  public String instanceListTotal(Integer instanceCount);

  /* Task Panel */
  @DefaultMessage("run once")
  public String runOnce();

  @DefaultMessage("repeat every {1} seconds until {0} times")
  public String repeatSeconds(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {1} minutes until {0} times")
  public String repeatMinutes(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {1} hours until {0} times")
  public String repeatHours(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {1} days until {0} times")
  public String repeatDays(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {1} months until {0} times")
  public String repeatMonths(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {1} years until {0} times")
  public String repeatYears(int repeatCount, long repeatInterval);

  @DefaultMessage("repeat every {0} seconds forever")
  public String repeatForeverSeconds(long repeatInterval);

  @DefaultMessage("repeat every {0} minutes forever")
  public String repeatForeverMinutes(long repeatInterval);

  @DefaultMessage("repeat every {0} hours forever")
  public String repeatForeverHours(long repeatInterval);

  @DefaultMessage("repeat every {0} days forever")
  public String repeatForeverDays(long repeatInterval);

  @DefaultMessage("repeat every {0} months forever")
  public String repeatForeverMonths(long repeatInterval);

  @DefaultMessage("repeat every {0} years forever")
  public String repeatForeverYears(long repeatInterval);

  @DefaultMessage("continuously repeat until {0} times")
  public String repeatContinuously(int repeatCount);

  @DefaultMessage("continuously repeat forever")
  public String repeatContinuouslyForever();

  /* Task Instance Panel */
  @DefaultMessage("{0}%")
  public String percentage(float completePercentage);

  /* Plugin Panel */
  @DefaultMessage("{0} (version {1})")
  public String pluginLabel(String pluginName, float pluginVersion);

}
