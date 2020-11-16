package org.roda.wui.client.disposal.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalScheduleDataPanel extends Composite implements HasValueChangeHandlers<DisposalSchedule> {

  interface MyUiBinder extends UiBinder<Widget, DisposalScheduleDataPanel> {
  }

  private static DisposalScheduleDataPanel.MyUiBinder uiBinder = GWT.create(DisposalScheduleDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox title;

  @UiField
  Label titleError;

  @UiField
  TextBox description;

  @UiField
  TextBox mandate;

  @UiField
  TextArea notes;

  // disposal actions

  @UiField
  Label disposalActionsLabel;

  @UiField
  ListBox disposalActions;

  @UiField
  Label disposalActionsError;

  // retention triggers elementId

  @UiField
  Label retentionTriggerElementIdLabel;

  @UiField
  ListBox retentionTriggerElementIdList;

  @UiField
  Label retentionTriggerElementIdError;

  // retention period interval

  @UiField
  Label retentionPeriodIntervalsLabel;

  @UiField
  ListBox retentionPeriodIntervals;

  @UiField
  Label retentionPeriodIntervalsError;

  // retention period duration

  @UiField
  FlowPanel retentionPeriodDurationPanel;

  @UiField
  Label retentionPeriodDurationLabel;

  @UiField
  TextBox retentionPeriodDuration;

  @UiField
  Label retentionPeriodDurationError;

  private final boolean editMode;

  private boolean changed = false;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalScheduleDataPanel(DisposalSchedule disposalSchedule, boolean editMode) {

    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;

    setInitialState();
    initHandlers();
    initList();

    if (editMode) {
      setEditInitialState();
      setDisposalSchedule(disposalSchedule);
    }
  }

  private void setEditInitialState() {
    disposalActionsLabel.setVisible(false);
    disposalActions.setVisible(false);
    disposalActionsError.setVisible(false);
  }

  private void initHandlers() {

    ChangeHandler changeHandler = event -> DisposalScheduleDataPanel.this.onChange();

    title.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);
    mandate.addChangeHandler(changeHandler);
    notes.addChangeHandler(changeHandler);

    ChangeHandler retentionPeriodChangeHandler = event -> {
      if (retentionPeriodIntervals.getSelectedValue()
        .equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.toString())) {
        retentionPeriodDurationPanel.removeStyleName("col_2");
        retentionPeriodDurationLabel.setVisible(false);
        retentionPeriodDuration.setVisible(false);
        retentionPeriodDuration.setValue("");
      } else {
        retentionPeriodDurationPanel.addStyleName("col_2");
        retentionPeriodDurationLabel.setVisible(true);
        retentionPeriodDurationLabel.setVisible(true);
        retentionPeriodDuration.setVisible(true);
      }
    };

    ChangeHandler disposalActionChangeHandler = event -> {

      if (disposalActions.getSelectedValue().equals(DisposalActionCode.RETAIN_PERMANENTLY.toString())
        || disposalActions.getSelectedValue().equals("")) {

        retentionTriggerElementIdLabel.setVisible(false);
        retentionTriggerElementIdList.setVisible(false);
        retentionTriggerElementIdList.setSelectedIndex(0);

        retentionPeriodIntervalsLabel.setVisible(false);
        retentionPeriodIntervals.setVisible(false);
        retentionPeriodIntervals.setSelectedIndex(0);

        retentionPeriodDurationLabel.setVisible(false);
        retentionPeriodDuration.setVisible(false);
        retentionPeriodDuration.setValue("");
      } else {
        retentionTriggerElementIdLabel.setVisible(true);
        retentionTriggerElementIdList.setVisible(true);
      }
    };

    ChangeHandler retentionTriggerElement = event -> {
      boolean value = true;
      if (retentionTriggerElementIdList.getSelectedValue().equals("")) {
        value = false;
      }
      retentionPeriodIntervalsLabel.setVisible(value);
      retentionPeriodIntervals.setVisible(value);
      retentionPeriodIntervals.setSelectedIndex(0);

      retentionPeriodDurationLabel.setVisible(value);
      retentionPeriodDuration.setVisible(value);
      retentionPeriodDuration.setValue("");
    };

    disposalActions.addChangeHandler(disposalActionChangeHandler);
    retentionPeriodIntervals.addChangeHandler(retentionPeriodChangeHandler);
    retentionTriggerElementIdList.addChangeHandler(retentionTriggerElement);
  }

  private void setInitialState() {
    errors.setVisible(false);

    retentionTriggerElementIdLabel.setVisible(false);
    retentionTriggerElementIdList.setVisible(false);

    retentionPeriodIntervalsLabel.setVisible(false);
    retentionPeriodIntervals.setVisible(false);

    retentionPeriodDurationLabel.setVisible(false);
    retentionPeriodDuration.setVisible(false);
  }

  private void initList() {
    List<DisposalActionCode> disposalActionCodes = Arrays.asList(DisposalActionCode.values());
    disposalActions.addItem("", "");
    for (DisposalActionCode actionCode : disposalActionCodes) {
      disposalActions.addItem(messages.disposalScheduleAction(actionCode.toString()), actionCode.toString());
    }

    List<RetentionPeriodIntervalCode> retentionPeriodIntervalCodes = Arrays
      .asList(RetentionPeriodIntervalCode.values());
    retentionPeriodIntervals.addItem("", "");
    for (RetentionPeriodIntervalCode retentionPeriodIntervalCode : retentionPeriodIntervalCodes) {
      retentionPeriodIntervals.addItem(
        messages.disposalScheduleRetentionPeriodIntervalValue(retentionPeriodIntervalCode.toString()),
        retentionPeriodIntervalCode.toString());
    }

    List<Pair<String, String>> retentionElementsIds = getElementsFromConfig();
    retentionTriggerElementIdList.addItem("", "");
    for (Pair<String, String> value : retentionElementsIds) {
      retentionTriggerElementIdList.addItem(value.getSecond(), value.getFirst());
    }
  }

  private static List<Pair<String, String>> getElementsFromConfig() {
    List<Pair<String, String>> elements = new ArrayList<>();
    String classSimpleName = IndexedAIP.class.getSimpleName();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.SEARCH_FIELD_PREFIX, classSimpleName);

    for (String field : fields) {
      String fieldPrefix = RodaConstants.SEARCH_FIELD_PREFIX + '.' + classSimpleName + '.' + field;
      String fieldType = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldsNames = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_FIELDS);

      if ((RodaConstants.SEARCH_FIELD_TYPE_DATE.equals(fieldType)
        || RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL.equals(fieldType))) {
        String fieldLabelI18N = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N);
        String translation = fieldLabelI18N;
        try {
          translation = ConfigurationManager.getTranslation(fieldLabelI18N);
        } catch (MissingResourceException e) {
          // do nothing;
        }

        Pair<String, String> pair = new Pair<>(fieldsNames, translation);
        elements.add(pair);
      }

    }
    return elements;
  }

  public void setDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.title.setText(disposalSchedule.getTitle());
    this.description.setText(disposalSchedule.getDescription());
    this.mandate.setText(disposalSchedule.getMandate());
    this.notes.setText(disposalSchedule.getScopeNotes());
  }

  public DisposalSchedule getDisposalSchedule() {
    DisposalSchedule disposalSchedule = new DisposalSchedule();
    disposalSchedule.setTitle(title.getText());
    disposalSchedule.setDescription(description.getText());
    disposalSchedule.setMandate(mandate.getText());
    disposalSchedule.setScopeNotes(notes.getText());
    if (!editMode) {
      disposalSchedule.setActionCode(getDisposalActionCode(disposalActions.getSelectedValue()));
      if (getDisposalActionCode(disposalActions.getSelectedValue()) != DisposalActionCode.RETAIN_PERMANENTLY) {
        disposalSchedule.setRetentionTriggerElementId(retentionTriggerElementIdList.getSelectedValue());
        disposalSchedule
          .setRetentionPeriodIntervalCode(getRetentionPeriodIntervalCode(retentionPeriodIntervals.getSelectedValue()));
        if (retentionPeriodDurationLabel.isVisible() && getRetentionPeriodIntervalCode(
          retentionPeriodIntervals.getSelectedValue()) != RetentionPeriodIntervalCode.NO_RETENTION_PERIOD) {
          disposalSchedule.setRetentionPeriodDuration(Integer.parseInt(retentionPeriodDuration.getText()));
        }
      }
    }
    return disposalSchedule;
  }

  private DisposalActionCode getDisposalActionCode(String string) {
    switch (string) {
      case "RETAIN_PERMANENTLY":
        return DisposalActionCode.RETAIN_PERMANENTLY;
      case "DESTROY":
        return DisposalActionCode.DESTROY;
      default:
        return DisposalActionCode.REVIEW;
    }
  }

  private RetentionPeriodIntervalCode getRetentionPeriodIntervalCode(String string) {
    switch (string) {
      case "NO_RETENTION_PERIOD":
        return RetentionPeriodIntervalCode.NO_RETENTION_PERIOD;
      case "YEARS":
        return RetentionPeriodIntervalCode.YEARS;
      case "MONTHS":
        return RetentionPeriodIntervalCode.MONTHS;
      case "WEEKS":
        return RetentionPeriodIntervalCode.WEEKS;
      default:
        return RetentionPeriodIntervalCode.DAYS;
    }
  }

  /**
   * Is disposal schedule data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    if (title.getText().length() == 0) {
      title.addStyleName("isWrong");
      titleError.setText(messages.mandatoryField());
      titleError.setVisible(true);
      Window.scrollTo(title.getAbsoluteLeft(), title.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalScheduleTitle()));
    } else {
      title.removeStyleName("isWrong");
      titleError.setVisible(false);
    }

    if (!editMode) {
      if (disposalActions.getSelectedValue().length() == 0) {
        disposalActions.addStyleName("isWrong");
        disposalActionsError.setText(messages.mandatoryField());
        disposalActionsError.setVisible(true);
        Window.scrollTo(disposalActions.getAbsoluteLeft(), disposalActions.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.disposalScheduleActionCol()));
      } else {
        disposalActions.removeStyleName("isWrong");
        disposalActionsError.setVisible(false);
      }

      if (!DisposalActionCode.RETAIN_PERMANENTLY.toString().equals(disposalActions.getSelectedValue())) {
        if (retentionTriggerElementIdList.getSelectedValue().length() == 0) {
          retentionTriggerElementIdList.addStyleName("isWrong");
          retentionTriggerElementIdError.setText(messages.mandatoryField());
          retentionTriggerElementIdError.setVisible(true);
          Window.scrollTo(retentionTriggerElementIdList.getAbsoluteLeft(),
            retentionTriggerElementIdList.getAbsoluteTop());
          errorList.add(messages.isAMandatoryField(messages.disposalScheduleRetentionTriggerElementId()));
        } else {
          retentionTriggerElementIdList.removeStyleName("isWrong");
          retentionTriggerElementIdError.setVisible(false);
        }

        if (retentionPeriodIntervals.getSelectedValue().length() == 0) {
          retentionPeriodIntervals.addStyleName("isWrong");
          retentionPeriodIntervalsError.setText(messages.mandatoryField());
          retentionPeriodIntervalsError.setVisible(true);
          Window.scrollTo(retentionPeriodIntervals.getAbsoluteLeft(), retentionPeriodIntervals.getAbsoluteTop());
          errorList.add(messages.isAMandatoryField(messages.disposalScheduleRetentionPeriodInterval()));
        } else {
          retentionPeriodIntervals.removeStyleName("isWrong");
          retentionPeriodIntervalsError.setVisible(false);
        }

        if (retentionPeriodDurationLabel.isVisible()) {
          if (!isNumberValid(retentionPeriodDuration.getText())) {
            retentionPeriodDuration.addStyleName("isWrong");
            retentionPeriodDurationError.setText(messages.mandatoryField());
            retentionPeriodDurationError.setVisible(true);
            Window.scrollTo(retentionPeriodDuration.getAbsoluteLeft(), retentionPeriodDuration.getAbsoluteTop());
            errorList.add(messages.disposalScheduleRetentionPeriodNotValidFormat());
          } else {
            retentionPeriodDuration.removeStyleName("isWrong");
            retentionPeriodDurationError.setVisible(false);
          }
        }
      }
    }

    checked = true;

    return errorList.isEmpty();
  }

  private boolean isNumberValid(String string) {
    boolean isNumber = true;
    try {
      int intNum = Integer.parseInt(string);
      if (intNum <= 0) {
        isNumber = false;
      }
    } catch (NumberFormatException e) {
      isNumber = false;
    }
    return isNumber;
  }

  public void clear() {
    title.setText("");
    description.setText("");
    mandate.setText("");
    notes.setText("");
    disposalActions.setSelectedIndex(0);
    retentionTriggerElementIdList.setSelectedIndex(0);
    retentionPeriodIntervals.setSelectedIndex(0);
    retentionPeriodDuration.setText("");
  }

  public boolean isEditMode() {
    return editMode;
  }

  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalSchedule> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public DisposalSchedule getValue() {
    return getDisposalSchedule();
  }
}
