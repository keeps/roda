package org.roda.wui.client.disposal.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
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

  public static final String IS_WRONG = "isWrong";

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
  ListBox retentionPeriodIntervals;

  // retention period duration
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

    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        DisposalScheduleDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        DisposalScheduleDataPanel.this.onChange();
      }
    };

    ChangeHandler retentionPeriodChangeHandler = event -> {
      if (retentionPeriodIntervals.getSelectedValue()
        .equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.toString())) {
        retentionPeriodDuration.setEnabled(false);
        retentionPeriodDuration.setValue("");
        retentionPeriodDuration.addStyleName("wui-cursor-not-allowed");
      } else {
        retentionPeriodDurationLabel.setVisible(true);
        retentionPeriodDurationLabel.setVisible(true);
        retentionPeriodDuration.setEnabled(true);
        retentionPeriodDuration.removeStyleName("wui-cursor-not-allowed");
      }
    };

    ChangeHandler disposalActionChangeHandler = event -> {

      if (disposalActions.getSelectedValue().equals(DisposalActionCode.RETAIN_PERMANENTLY.name())
        || disposalActions.getSelectedIndex() == 0) {
        retentionTriggerElementIdLabel.setVisible(false);
        retentionTriggerElementIdList.setVisible(false);
        retentionTriggerElementIdList.setSelectedIndex(0);

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
      retentionPeriodIntervals.setVisible(value);
      retentionPeriodIntervals.setSelectedIndex(0);

      retentionPeriodDurationLabel.setVisible(value);
      retentionPeriodDuration.setVisible(value);
      retentionPeriodDuration.setValue("");
    };

    disposalActions.addChangeHandler(disposalActionChangeHandler);
    retentionPeriodIntervals.addChangeHandler(retentionPeriodChangeHandler);
    retentionTriggerElementIdList.addChangeHandler(retentionTriggerElement);

    title.addChangeHandler(changeHandler);
    title.addKeyUpHandler(keyUpHandler);
    disposalActions.addChangeHandler(changeHandler);
    retentionTriggerElementIdList.addChangeHandler(changeHandler);
    retentionPeriodDuration.addKeyUpHandler(keyUpHandler);
    retentionPeriodDuration.addChangeHandler(changeHandler);
    retentionPeriodIntervals.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);
    mandate.addChangeHandler(changeHandler);
    notes.addChangeHandler(changeHandler);
  }

  private void setInitialState() {
    errors.setVisible(false);

    retentionTriggerElementIdLabel.setVisible(false);
    retentionTriggerElementIdList.setVisible(false);

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

    List<Pair<String, String>> retentionElementsIds = DisposalScheduleUtils.getElementsFromConfig();
    retentionTriggerElementIdList.addItem("", "");
    for (Pair<String, String> value : retentionElementsIds) {
      retentionTriggerElementIdList.addItem(value.getSecond(), value.getFirst());
    }
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
        if (retentionPeriodDurationLabel.isVisible() && StringUtils.isNotBlank(retentionPeriodDuration.getText())) {
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
      case "DAYS":
        return RetentionPeriodIntervalCode.DAYS;
      default:
        return null;
    }
  }

  /**
   * Is disposal schedule data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    if (StringUtils.isBlank(title.getText())) {
      title.addStyleName(IS_WRONG);
      titleError.setText(messages.mandatoryField());
      titleError.setVisible(true);
      Window.scrollTo(title.getAbsoluteLeft(), title.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalScheduleTitle()));
    } else {
      title.removeStyleName(IS_WRONG);
      titleError.setVisible(false);
    }

    if (!editMode) {
      if (disposalActions.getSelectedIndex() == 0) {
        disposalActions.addStyleName(IS_WRONG);
        disposalActionsError.setText(messages.mandatoryField());
        disposalActionsError.setVisible(true);
        Window.scrollTo(disposalActions.getAbsoluteLeft(), disposalActions.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.disposalScheduleActionCol()));
      } else {
        disposalActions.removeStyleName(IS_WRONG);
        disposalActionsError.setVisible(false);
      }

      if (retentionTriggerElementIdList.isVisible()) {
        if (retentionTriggerElementIdList.getSelectedIndex() == 0) {
          retentionTriggerElementIdList.addStyleName(IS_WRONG);
          retentionTriggerElementIdError.setText(messages.mandatoryField());
          retentionTriggerElementIdError.setVisible(true);
          Window.scrollTo(retentionTriggerElementIdList.getAbsoluteLeft(),
            retentionTriggerElementIdList.getAbsoluteTop());
          errorList.add(messages.isAMandatoryField(messages.disposalScheduleRetentionTriggerElementId()));
        } else {
          retentionTriggerElementIdList.removeStyleName(IS_WRONG);
          retentionTriggerElementIdError.setVisible(false);
        }
      } else {
        retentionTriggerElementIdList.removeStyleName(IS_WRONG);
        retentionTriggerElementIdError.setVisible(false);
        retentionPeriodDuration.removeStyleName(IS_WRONG);
        retentionPeriodDurationError.setVisible(false);
      }

      if (retentionPeriodDurationLabel.isVisible()) {
        if (retentionPeriodIntervals.getSelectedIndex() == 0) {
          retentionPeriodIntervals.addStyleName(IS_WRONG);
          retentionPeriodDuration.addStyleName(IS_WRONG);
          retentionPeriodDurationError.setText(messages.mandatoryField());
          retentionPeriodDurationError.setVisible(true);
          Window.scrollTo(retentionPeriodIntervals.getAbsoluteLeft(), retentionPeriodIntervals.getAbsoluteTop());
          errorList.add(messages.isAMandatoryField(messages.disposalScheduleRetentionPeriodInterval()));
        } else if (RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.toString()
          .equals(retentionPeriodIntervals.getSelectedValue())) {
          retentionPeriodIntervals.removeStyleName(IS_WRONG);
          retentionPeriodDuration.removeStyleName(IS_WRONG);
          retentionPeriodDurationError.setVisible(false);
        } else {
          if (!isNumberValid(retentionPeriodDuration.getText())) {
            retentionPeriodDuration.addStyleName(IS_WRONG);
            retentionPeriodDurationError.setText(messages.numberIsRequired());
            retentionPeriodIntervals.removeStyleName(IS_WRONG);
            retentionPeriodDurationError.setVisible(true);
            Window.scrollTo(retentionPeriodIntervals.getAbsoluteLeft(), retentionPeriodIntervals.getAbsoluteTop());
            errorList.add(messages.isAMandatoryField(messages.disposalScheduleRetentionPeriodInterval()));
          } else {
            retentionPeriodDuration.removeStyleName(IS_WRONG);
            retentionPeriodIntervals.removeStyleName(IS_WRONG);
            retentionPeriodDurationError.setVisible(false);
          }
        }
      } else {
        retentionPeriodDuration.removeStyleName(IS_WRONG);
        retentionPeriodIntervals.removeStyleName(IS_WRONG);
        retentionPeriodDurationError.setVisible(false);
      }
    }

    checked = true;

    if (!errorList.isEmpty()) {
      errors.setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append("<span class='error'>").append(error).append("</span>");
        errorString.append("<br/>");
      }
      errors.setHTML(errorString.toString());
    } else {
      errors.setVisible(false);
    }

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
    GWT.log("HERE!!!");
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
