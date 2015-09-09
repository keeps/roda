/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client;

import java.util.Date;

import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.DateTimePicker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class TaskDataPanel {

  private static EventManagementConstants constants = (EventManagementConstants) GWT
    .create(EventManagementConstants.class);

  protected static String REPEAT_INTERVAL_MAGNITUDE_SECONDS = "SECONDS";
  protected static String REPEAT_INTERVAL_MAGNITUDE_MINUTES = "MINUTES";
  protected static String REPEAT_INTERVAL_MAGNITUDE_HOURS = "HOURS";
  protected static String REPEAT_INTERVAL_MAGNITUDE_DAYS = "DAYS";
  protected static String REPEAT_INTERVAL_MAGNITUDE_MONTHS = "MONTHS";
  protected static String REPEAT_INTERVAL_MAGNITUDE_YEARS = "YEARS";

  protected static String REPEAT_UNTIL_FOREVER = "FOREVER";
  protected static String REPEAT_UNTIL_REPETITIONS = "REPETITIONS";

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private Task task;

  private final FlexTable layout;
  private final Label nameLabel;
  private final TextBox nameBox;
  private final Label descriptionLabel;
  private final TextArea descriptionBox;
  private final Label startDateLabel;
  private final StartDatePanel startDatePanel;
  private final Label repeatLabel;
  private final RepeatPanel repeatPanel;
  private final Label pluginNameLabel;
  private final PluginPanel pluginPanel;

  /**
   * Create a new task data panel
   */
  public TaskDataPanel() {
    task = new Task();
    layout = new FlexTable();

    nameLabel = new Label(constants.taskName());
    descriptionLabel = new Label(constants.taskDescription());
    startDateLabel = new Label(constants.taskStartDate());
    repeatLabel = new Label(constants.taskRepeat());
    pluginNameLabel = new Label(constants.taskPluginName());

    nameBox = new TextBox();
    descriptionBox = new TextArea();
    startDatePanel = new StartDatePanel();
    repeatPanel = new RepeatPanel();
    pluginPanel = new PluginPanel();

    layout.setWidget(0, 0, nameLabel);
    layout.setWidget(0, 1, nameBox);
    layout.setWidget(1, 0, descriptionLabel);
    layout.setWidget(1, 1, descriptionBox);
    layout.setWidget(2, 0, startDateLabel);
    layout.setWidget(2, 1, startDatePanel.getWidget());
    layout.setWidget(3, 0, repeatLabel);
    layout.setWidget(3, 1, repeatPanel.getWidget());
    layout.setWidget(4, 0, pluginNameLabel);
    layout.setWidget(4, 1, pluginPanel.getWidget());

    layout.getCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
    layout.getCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
    layout.getCellFormatter().setVerticalAlignment(2, 0, HasAlignment.ALIGN_TOP);
    layout.getCellFormatter().setVerticalAlignment(3, 0, HasAlignment.ALIGN_TOP);
    layout.getCellFormatter().setVerticalAlignment(4, 0, HasAlignment.ALIGN_TOP);

    layout.getCellFormatter().setWidth(0, 1, "100%");
    layout.getCellFormatter().setWidth(1, 1, "100%");
    layout.getCellFormatter().setWidth(2, 1, "100%");
    layout.getCellFormatter().setWidth(3, 1, "100%");
    layout.getCellFormatter().setWidth(4, 1, "100%");

    layout.addStyleName("wui-task-data");
    nameLabel.addStyleName("task-name-label");
    descriptionLabel.addStyleName("task-description-label");
    startDateLabel.addStyleName("task-startDate-label");
    repeatLabel.addStyleName("task-repeat-label");
    pluginNameLabel.addStyleName("task-plugin-label");

    nameBox.addStyleName("task-name-box");
    descriptionBox.addStyleName("task-description-box");

  }

  /**
   * Get task as defined in the panel
   * 
   * @return
   */
  public Task getTask() {
    task.setName(nameBox.getText());
    task.setDescription(descriptionBox.getText());
    task.setStartDate(startDatePanel.getDate());
    task.setRepeatCount(repeatPanel.getRepeatCount());
    task.setRepeatInterval(repeatPanel.getRepeatInterval());
    task.setPluginInfo(pluginPanel.getSelected());

    return task;
  }

  /**
   * Set task defined in the panel
   * 
   * @param task
   */
  public void setTask(Task task) {
    this.task = task;

    logger.debug("Setting task " + task);

    nameBox.setText(task.getName());
    descriptionBox.setText(task.getDescription());
    startDatePanel.setDate(task.getStartDate());
    repeatPanel.setRepeat(task.getRepeatCount(), task.getRepeatInterval());
    pluginPanel.setSelected(task.getPluginInfo());
  }

  /**
   * Get task data panel widget
   * 
   * @return
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Check is task data is valid
   * 
   * @return
   */
  public boolean isValid() {
    boolean valid = true;
    if (nameBox.getText().length() == 0) {
      valid = false;
    } else {
      valid &= startDatePanel.isValid();
      valid &= repeatPanel.isValid();
      valid &= pluginPanel.isValid();
    }
    return valid;
  }

  /**
   * Set selected plugin
   * 
   * @param index
   *          the index of the plugin in the plugin list
   */
  public void setSelectedPlugin(int index) {
    pluginPanel.setSelected(index);
  }

  private class StartDatePanel {
    private final VerticalPanel layout;
    private final RadioButton currentDateRadio;
    private final HorizontalPanel scheduleLayout;
    private final RadioButton scheduleDateRadio;
    private final DateTimePicker scheduleDatePicker;

    /**
     * Create a new start date panel
     */
    public StartDatePanel() {
      layout = new VerticalPanel();
      currentDateRadio = new RadioButton("task-start-date", constants.taskStartDateNow());
      scheduleLayout = new HorizontalPanel();
      scheduleDateRadio = new RadioButton("task-start-date", constants.taskStartDateSchedule());
      scheduleDatePicker = new DateTimePicker();
      // scheduleDatePicker.setDate(new Date());

      layout.add(currentDateRadio);
      layout.add(scheduleLayout);

      scheduleLayout.add(scheduleDateRadio);
      scheduleLayout.add(scheduleDatePicker.getWidget());

      currentDateRadio.setChecked(true);
      scheduleDatePicker.getWidget().setVisible(false);

      ClickListener radioListener = new ClickListener() {

        public void onClick(Widget sender) {
          scheduleDatePicker.getWidget().setVisible(scheduleDateRadio.isChecked());
        }

      };

      currentDateRadio.addClickListener(radioListener);
      scheduleDateRadio.addClickListener(radioListener);

      layout.addStyleName("wui-task-startDate");
      currentDateRadio.addStyleName("task-startDate-current");
      scheduleLayout.addStyleName("task-startDate-schedule-layout");
      scheduleDateRadio.addStyleName("task-startDate-schedule");
      scheduleDatePicker.getWidget().addStyleName("task-startDate-schedule-picker");

    }

    /**
     * Get date as defined in the panel. If current date is selected, null will
     * be returned.
     * 
     * @return
     */
    public Date getDate() {
      Date date;
      if (currentDateRadio.isChecked()) {
        date = null;
      } else {
        date = scheduleDatePicker.getDate();
      }
      return date;
    }

    /**
     * Set date defined in the panel. If date is null, current date will be
     * selected
     * 
     * @param date
     */
    public void setDate(Date date) {
      if (date == null) {
        currentDateRadio.setChecked(true);
        scheduleDatePicker.getWidget().setVisible(false);
      } else {
        scheduleDateRadio.setChecked(true);
        scheduleDatePicker.setDate(date);
        scheduleDatePicker.getWidget().setVisible(true);
      }
    }

    /**
     * Get the widget
     * 
     * @return
     */
    public Widget getWidget() {
      return layout;
    }

    /**
     * Check if start date is correctly filled
     * 
     * @return
     */
    public boolean isValid() {
      boolean valid = true;
      if (scheduleDateRadio.isChecked() && scheduleDatePicker.getDate() == null) {
        valid = false;
      }
      return valid;
    }
  }

  /**
   * Repeat Panel
   * 
   */
  private class RepeatPanel {

    private VerticalPanel layout;
    private RadioButton executeOnlyOnce;
    private RadioButton repeat;
    private HorizontalPanel repeatLayout;
    private HorizontalPanel repeatCenterLayout;
    private HorizontalPanel intervalLayout;
    private Label intervalLabel;
    private TextBox intervalBox;
    private ListBox intervalMagnitude;
    private HorizontalPanel untilLayout;
    private Label untilLabel;
    private TextBox untilBox;
    private ListBox untilType;

    /**
     * Create a new repeat panel
     */
    public RepeatPanel() {
      layout = new VerticalPanel();
      executeOnlyOnce = new RadioButton("event-repeat", constants.taskExecuteOnlyOnce());
      repeat = new RadioButton("event-repeat", constants.taskDoRepeat());

      repeatLayout = new HorizontalPanel();
      repeatCenterLayout = new HorizontalPanel();

      intervalLayout = new HorizontalPanel();
      intervalLabel = new Label(constants.taskRepeatIntervalLabel());
      intervalBox = new TextBox();
      intervalMagnitude = new ListBox();
      intervalMagnitude.setVisibleItemCount(1);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeSeconds(), REPEAT_INTERVAL_MAGNITUDE_SECONDS);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeMinutes(), REPEAT_INTERVAL_MAGNITUDE_MINUTES);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeHours(), REPEAT_INTERVAL_MAGNITUDE_HOURS);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeDays(), REPEAT_INTERVAL_MAGNITUDE_DAYS);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeMonths(), REPEAT_INTERVAL_MAGNITUDE_MONTHS);
      intervalMagnitude.addItem(constants.taskIntervalMagnitudeYears(), REPEAT_INTERVAL_MAGNITUDE_YEARS);

      untilLayout = new HorizontalPanel();
      untilLabel = new Label(constants.taskRepeatUntil());
      untilBox = new TextBox();
      untilType = new ListBox();
      untilType.setVisibleItemCount(1);
      untilType.addItem(constants.taskRepeatUntilForever(), REPEAT_UNTIL_FOREVER);
      untilType.addItem(constants.taskRepeatUntilRepetitions(), REPEAT_UNTIL_REPETITIONS);

      layout.add(executeOnlyOnce);
      layout.add(repeatLayout);

      repeatLayout.add(repeat);
      repeatLayout.add(repeatCenterLayout);

      repeatCenterLayout.add(intervalLayout);
      repeatCenterLayout.add(untilLayout);

      intervalLayout.add(intervalLabel);
      intervalLayout.add(intervalBox);
      intervalLayout.add(intervalMagnitude);

      untilLayout.add(untilLabel);
      untilLayout.add(untilBox);
      untilLayout.add(untilType);

      untilBox.setVisible(false);

      untilType.addChangeListener(new ChangeListener() {

        public void onChange(Widget sender) {
          if (untilType.getValue(untilType.getSelectedIndex()).equals(REPEAT_UNTIL_REPETITIONS)) {
            untilBox.setVisible(true);
          } else /* if REPEAT_UNTIL_FOREVER */{
            untilBox.setVisible(false);

          }

        }

      });

      executeOnlyOnce.setChecked(true);
      repeatCenterLayout.setVisible(false);

      ClickListener radioListener = new ClickListener() {

        public void onClick(Widget sender) {
          repeatCenterLayout.setVisible(repeat.isChecked());

        }

      };

      executeOnlyOnce.addClickListener(radioListener);
      repeat.addClickListener(radioListener);

      layout.addStyleName("wui-task-repeat");
      executeOnlyOnce.addStyleName("task-repeat-not");
      repeat.addStyleName("task-repeat-yes");
      repeatLayout.addStyleName("task-repeat-layout");
      repeatCenterLayout.addStyleName("task-repeat-layout-center");
      intervalLayout.addStyleName("task-repeat-interval-layout");
      intervalLabel.addStyleName("task-repeat-interval-label");
      intervalBox.addStyleName("task-repeat-interval-box");
      intervalMagnitude.addStyleName("task-repeat-interval-magnitude");
      untilLayout.addStyleName("task-repeat-until-layout");
      untilLabel.addStyleName("task-repeat-until-label");
      untilBox.addStyleName("task-repeat-until-box");
      untilType.addStyleName("task-repeat-until-type");
    }

    /**
     * Get repeat count
     * 
     * @return returns 0 if the action is executed only once, more than 0 if the
     *         action will be repeated a defined number of times or -1 if action
     *         will be repeated forever
     */
    public int getRepeatCount() {
      int count;
      if (executeOnlyOnce.isChecked()) {
        count = 0;
      } else if (untilType.getValue(untilType.getSelectedIndex()).equals(REPEAT_UNTIL_FOREVER)) {
        count = -1;
      } else /* if REPEAT_UNTIL_REPETITIONS */{
        count = Integer.valueOf(untilBox.getText());
      }
      return count;
    }

    /**
     * The time between repetitions in seconds
     * 
     * @return
     */
    public long getRepeatInterval() {
      int interval;
      int magnitude;

      if (!repeat.isChecked() || intervalBox.getText().length() == 0) {
        interval = 0;

      } else {
        interval = Integer.valueOf(intervalBox.getText());
      }

      if (intervalMagnitude.getValue(intervalMagnitude.getSelectedIndex()).equals(REPEAT_INTERVAL_MAGNITUDE_SECONDS)) {
        magnitude = 1;
      } else if (intervalMagnitude.getValue(intervalMagnitude.getSelectedIndex()).equals(
        REPEAT_INTERVAL_MAGNITUDE_MINUTES)) {
        magnitude = 60;
      } else if (intervalMagnitude.getValue(intervalMagnitude.getSelectedIndex()).equals(
        REPEAT_INTERVAL_MAGNITUDE_HOURS)) {
        magnitude = 3600;
      } else if (intervalMagnitude.getValue(intervalMagnitude.getSelectedIndex())
        .equals(REPEAT_INTERVAL_MAGNITUDE_DAYS)) {
        magnitude = 86400;
      } else if (intervalMagnitude.getValue(intervalMagnitude.getSelectedIndex()).equals(
        REPEAT_INTERVAL_MAGNITUDE_MONTHS)) {
        magnitude = 2678400;
      } else /* if REPEAT_INTERVAL_MAGNITUDE_YEARS */{
        magnitude = 31557600;
      }
      return interval * magnitude;
    }

    /**
     * Set the repetitions
     * 
     * @param count
     *          the repeat count, use -1 to repeat forever
     * @param interval
     *          the repeat interval in seconds
     */
    public void setRepeat(int count, long interval) {
      if (count == 0) {
        executeOnlyOnce.setChecked(true);
      } else {
        repeat.setChecked(true);
        repeatCenterLayout.setVisible(true);
        if (count == -1) {
          setSelectedValue(untilType, REPEAT_UNTIL_FOREVER);
          untilBox.setVisible(false);
        } else {
          setSelectedValue(untilType, REPEAT_UNTIL_REPETITIONS);
          untilBox.setText("" + count);
          untilBox.setVisible(true);
        }
        if (interval % 31557600 == 0) {
          intervalBox.setText("" + interval / 31557600);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_YEARS);
        } else if (interval % 2678400 == 0) {
          intervalBox.setText("" + interval / 2678400);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_MONTHS);
        } else if (interval % 86400 == 0) {
          intervalBox.setText("" + interval / 86400);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_DAYS);
        } else if (interval % 3600 == 0) {
          intervalBox.setText("" + interval / 3600);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_HOURS);
        } else if (interval % 60 == 0) {
          intervalBox.setText("" + interval / 60);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_MINUTES);
        } else /* SECONDS */{
          intervalBox.setText("" + interval);
          setSelectedValue(intervalMagnitude, REPEAT_INTERVAL_MAGNITUDE_SECONDS);
        }
      }
    }

    protected void setSelectedValue(ListBox listBox, String value) {
      for (int i = 0; i < listBox.getItemCount(); i++) {
        if (listBox.getValue(i).equals(value)) {
          listBox.setSelectedIndex(i);
          break;
        }
      }
    }

    /**
     * Get widget
     * 
     * @return
     */
    public Widget getWidget() {
      return layout;
    }

    /**
     * Check if repeat data is correctly inserted
     * 
     * @return
     */
    public boolean isValid() {
      boolean valid = true;
      if (repeat.isChecked()) {
        if (intervalBox.getText().length() == 0) {
          valid = false;
        } else if (untilType.getValue(untilType.getSelectedIndex()).equals(REPEAT_UNTIL_REPETITIONS)
          && untilBox.getText().length() == 0) {
          valid = false;
        } else if (getRepeatInterval() <= 0) {
          valid = false;
        } else if (untilType.getValue(untilType.getSelectedIndex()).equals(REPEAT_UNTIL_REPETITIONS)
          && getRepeatCount() <= 0) {
          valid = false;
        }
      }
      return valid;
    }

  }

}
