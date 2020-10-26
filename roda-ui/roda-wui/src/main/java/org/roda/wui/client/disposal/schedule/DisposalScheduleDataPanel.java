package org.roda.wui.client.disposal.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.ip.disposal.RetentionTriggerCode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
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
  ListBox disposalActions;

  @UiField
  Label disposalActionsError;

  // retention triggers

  @UiField
  Label retentionTriggersLabel;

  @UiField
  ListBox retentionTriggers;

  @UiField
  Label retentionTriggersError;

  // retention period interval

  @UiField
  Label retentionPeriodIntervalsLabel;

  @UiField
  ListBox retentionPeriodIntervals;

  @UiField
  Label retentionPeriodIntervalsError;

  // retention period duration

  @UiField
  Label retentionPeriodDurationLabel;

  @UiField
  TextBox retentionPeriodDuration;

  @UiField
  Label retentionPeriodDurationError;

  private boolean editmode;

  // has to be true to detected new field changes
  private boolean changed = true;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalScheduleDataPanel(boolean visible, boolean editmode) {

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);

    setInitialState();
    initList();

    ValueChangeHandler<String> valueChangedHandler = new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        DisposalScheduleDataPanel.this.onChange();
      }
    };

    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        DisposalScheduleDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    ChangeHandler retentionTriggerChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {

        if (retentionTriggers.getSelectedValue().equals("")) {
          retentionPeriodIntervalsLabel.setVisible(false);
          retentionPeriodIntervals.setVisible(false);
          retentionPeriodIntervals.setSelectedIndex(0);

          retentionPeriodDurationLabel.setVisible(false);
          retentionPeriodDuration.setVisible(false);
          retentionPeriodDuration.setValue("");
        } else {
          retentionPeriodIntervalsLabel.setVisible(true);
          retentionPeriodIntervals.setVisible(true);
        }
      }
    };

    ChangeHandler retentionPeriodChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (retentionPeriodIntervals.getSelectedValue()
          .equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.toString())
          || retentionPeriodIntervals.getSelectedValue().equals("")) {
          retentionPeriodDurationLabel.setVisible(false);
          retentionPeriodDuration.setVisible(false);
          retentionPeriodDuration.setValue("");
        } else {
          retentionPeriodDurationLabel.setVisible(true);
          retentionPeriodDuration.setVisible(true);
        }
      }
    };

    ChangeHandler disposalActionChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {

        if (disposalActions.getSelectedValue().equals(DisposalActionCode.RETAIN_PERMANENTLY.toString())
          || disposalActions.getSelectedValue().equals("")) {
          retentionTriggersLabel.setVisible(false);
          retentionTriggers.setVisible(false);
          retentionTriggers.setSelectedIndex(0);

          retentionPeriodIntervalsLabel.setVisible(false);
          retentionPeriodIntervals.setVisible(false);
          retentionPeriodIntervals.setSelectedIndex(0);

          retentionPeriodDurationLabel.setVisible(false);
          retentionPeriodDuration.setVisible(false);
          retentionPeriodDuration.setValue("");
        } else {
          retentionTriggersLabel.setVisible(true);
          retentionTriggers.setVisible(true);
        }
      }
    };

    disposalActions.addChangeHandler(disposalActionChangeHandler);
    retentionTriggers.addChangeHandler(retentionTriggerChangeHandler);
    retentionPeriodIntervals.addChangeHandler(retentionPeriodChangeHandler);
  }

  private void setInitialState() {
    errors.setVisible(false);

    retentionTriggersLabel.setVisible(false);
    retentionTriggers.setVisible(false);

    retentionPeriodIntervalsLabel.setVisible(false);
    retentionPeriodIntervals.setVisible(false);

    retentionPeriodDurationLabel.setVisible(false);
    retentionPeriodDuration.setVisible(false);
  }

  private void initList() {
    List<DisposalActionCode> disposalActionCodes = Arrays.asList(DisposalActionCode.values());
    disposalActions.addItem("", "");
    for (DisposalActionCode actionCode : disposalActionCodes) {
      disposalActions.addItem(actionCode.toString(), actionCode.toString());
    }

    List<RetentionTriggerCode> retentionTriggerCodes = Arrays.asList(RetentionTriggerCode.values());
    retentionTriggers.addItem("", "");
    for (RetentionTriggerCode retentionTriggerCode : retentionTriggerCodes) {
      retentionTriggers.addItem(retentionTriggerCode.toString(), retentionTriggerCode.toString());
    }

    List<RetentionPeriodIntervalCode> retentionPeriodIntervalCodes = Arrays
      .asList(RetentionPeriodIntervalCode.values());
    retentionPeriodIntervals.addItem("", "");
    for (RetentionPeriodIntervalCode retentionPeriodIntervalCode : retentionPeriodIntervalCodes) {
      retentionPeriodIntervals.addItem(retentionPeriodIntervalCode.toString(), retentionPeriodIntervalCode.toString());
    }
  }

  public void setDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.title.setText(disposalSchedule.getTitle());
  }

  public DisposalSchedule getDisposalSchedule() {
    DisposalSchedule disposalSchedule = new DisposalSchedule();
    disposalSchedule.setTitle(title.getText());
    disposalSchedule.setDescription(description.getText());
    disposalSchedule.setMandate(mandate.getText());
    disposalSchedule.setScopeNotes(notes.getText());
    disposalSchedule.setActionCode(getDisposalActionCode(disposalActions.getSelectedValue()));
    if (getDisposalActionCode(disposalActions.getSelectedValue()) != DisposalActionCode.RETAIN_PERMANENTLY) {
      disposalSchedule.setRetentionTriggerCode(getRetentionTriggerCode(retentionTriggers.getSelectedValue()));
      disposalSchedule
        .setRetentionPeriodIntervalCode(getRetentionPeriodIntervalCode(retentionTriggers.getSelectedValue()));
      disposalSchedule.setRetentionPeriodDuration(Integer.parseInt(retentionPeriodDuration.getText()));
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

  private RetentionTriggerCode getRetentionTriggerCode(String string) {
    switch (string) {
      case "FROM_RECORD_ORIGINATED_DATE":
        return RetentionTriggerCode.FROM_RECORD_ORIGINATED_DATE;
      case "FROM_RECORD_METADATA_DATE":
        return RetentionTriggerCode.FROM_RECORD_METADATA_DATE;
      default:
        return RetentionTriggerCode.FROM_NOW;
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
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      title.removeStyleName("isWrong");
      titleError.setVisible(false);
    }

    if (disposalActions.getSelectedValue().length() == 0) {
      disposalActions.addStyleName("isWrong");
      disposalActionsError.setText(messages.mandatoryField());
      disposalActionsError.setVisible(true);
      Window.scrollTo(disposalActions.getAbsoluteLeft(), disposalActions.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      disposalActions.removeStyleName("isWrong");
      disposalActionsError.setVisible(false);
    }

    if (disposalActions.getSelectedValue() != DisposalActionCode.RETAIN_PERMANENTLY.toString()) {
      if (retentionTriggers.getSelectedValue().length() == 0) {
        retentionTriggers.addStyleName("isWrong");
        retentionTriggersError.setText(messages.mandatoryField());
        retentionTriggersError.setVisible(true);
        Window.scrollTo(retentionTriggers.getAbsoluteLeft(), retentionTriggers.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.username()));
      } else {
        retentionTriggers.removeStyleName("isWrong");
        retentionTriggersError.setVisible(false);
      }

      if (retentionPeriodIntervals.getSelectedValue().length() == 0) {
        retentionPeriodIntervals.addStyleName("isWrong");
        retentionPeriodIntervalsError.setText(messages.mandatoryField());
        retentionPeriodIntervalsError.setVisible(true);
        Window.scrollTo(retentionPeriodIntervals.getAbsoluteLeft(), retentionPeriodIntervals.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.username()));
      } else {
        retentionPeriodIntervals.removeStyleName("isWrong");
        retentionPeriodIntervalsError.setVisible(false);
      }

      if (retentionPeriodDuration.isVisible()) {
        if (!isNumberValid(retentionPeriodDuration.getText())) {
          retentionPeriodDuration.addStyleName("isWrong");
          retentionPeriodDurationError.setText(messages.mandatoryField());
          retentionPeriodDurationError.setVisible(true);
          Window.scrollTo(retentionPeriodDuration.getAbsoluteLeft(), retentionPeriodDuration.getAbsoluteTop());
          errorList.add(messages.isAMandatoryField(messages.username()));
        } else {
          retentionPeriodDuration.removeStyleName("isWrong");
          retentionPeriodDurationError.setVisible(false);
        }
      }
    }

    checked = true;

    return errorList.isEmpty() ? true : false;
  }

  private boolean isNumberValid(String string) {
    boolean isNumber = true;
    try {
      Integer intNum = Integer.parseInt(string);
      if (intNum > 0) {
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      isNumber = false;
    } finally {
      return isNumber;
    }
  }

  public boolean isTitleReadOnly() {
    return title.isReadOnly();
  }

  public void setTitleReadOnly(boolean readonly) {
    title.setReadOnly(readonly);
  }

  public boolean isMandateReadOnly() {
    return mandate.isReadOnly();
  }

  public void setMandateReadOnly(boolean readonly) {
    mandate.setReadOnly(readonly);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
  }

  public void clear() {
    title.setText("");
    description.setText("");
    mandate.setText("");
  }

  public boolean isEditmode() {
    return editmode;
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
