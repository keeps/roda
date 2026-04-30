package org.roda.wui.client.disposal.schedule.data.panels;

import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.client.disposal.schedule.DisposalScheduleUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalScheduleDataPanel extends Composite
  implements GenericDataPanel<DisposalSchedule>, HasValueChangeHandlers<DisposalSchedule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<DisposalSchedule> form;
  private final boolean editMode;

  private final Button saveButton;
  private final Button cancelButton;

  public DisposalScheduleDataPanel(DisposalSchedule disposalSchedule, boolean editMode) {
    this.editMode = editMode;
    this.form = new GenericDataForm<>();

    if (editMode) {
      initEditMode(disposalSchedule);
    } else {
      initCreateMode(disposalSchedule);
    }

    // 1. Initialize Buttons
    saveButton = new Button(messages.saveButton());
    saveButton.addStyleName("btn btn-primary btn-play");

    cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");

    // 2. Wrap buttons in a FlowPanel for spacing
    FlowPanel actionsPanel = new FlowPanel();
    actionsPanel.addStyleName("alignButtonsPanel"); // Uses your existing CSS spacing
    actionsPanel.add(saveButton);
    actionsPanel.add(cancelButton);

    // 3. Inject the buttons at the bottom of the generic form
    form.addCustomWidget(actionsPanel);

    // Initialize the composite using the generic form as the root widget
    initWidget(form);
  }

  private void initEditMode(DisposalSchedule disposalSchedule) {
    form.addTextField(messages.disposalScheduleTitle(), DisposalSchedule::getTitle, DisposalSchedule::setTitle, true);
    form.addTextField(messages.disposalScheduleDescription(), DisposalSchedule::getDescription,
      DisposalSchedule::setDescription, false);
    form.addTextArea(messages.disposalScheduleMandate(), DisposalSchedule::getMandate, DisposalSchedule::setMandate,
      false);
    form.addTextArea(messages.disposalScheduleNotes(), DisposalSchedule::getScopeNotes, DisposalSchedule::setScopeNotes,
      false);

    form.addReadOnlyField(messages.disposalActionLabel(),
      s -> messages.disposalScheduleActionCode(s.getActionCode().toString()), false);
    if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(disposalSchedule.getActionCode())) {
      form.addReadOnlyField(messages.disposalScheduleRetentionTriggerElementId(),
        s -> DisposalScheduleUtils.getI18nRetentionTriggerIdentifier(s.getRetentionTriggerElementId()), false);
      form.addReadOnlyField(messages.disposalScheduleRetentionPeriodDuration(), s -> {
        if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(s.getActionCode())) {
          String retentionPeriod;
          if (s.getRetentionPeriodIntervalCode().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
            retentionPeriod = messages.retentionPeriod(0, s.getRetentionPeriodIntervalCode().toString());
          } else {
            retentionPeriod = messages.retentionPeriod(s.getRetentionPeriodDuration(),
              s.getRetentionPeriodIntervalCode().toString());
          }
          return retentionPeriod;
        } else {
          return "";
        }
      }, false);
    }

    form.setModel(disposalSchedule);
  }

  private void initCreateMode(DisposalSchedule disposalSchedule) {
    form.addTextField(messages.disposalScheduleTitle(), DisposalSchedule::getTitle, DisposalSchedule::setTitle, true);
    form.addTextField(messages.disposalScheduleDescription(), DisposalSchedule::getDescription,
      DisposalSchedule::setDescription, false);
    form.addTextArea(messages.disposalScheduleMandate(), DisposalSchedule::getMandate, DisposalSchedule::setMandate,
      false);
    form.addTextArea(messages.disposalScheduleNotes(), DisposalSchedule::getScopeNotes, DisposalSchedule::setScopeNotes,
      false);

    // 4. Actions (ListBox)
    ListBox actionList = new ListBox();
    actionList.addItem("", "");
    for (DisposalActionCode code : DisposalActionCode.values()) {
      actionList.addItem(messages.disposalScheduleAction(code.name()), code.name());
    }
    FlowPanel actionRow = form.addListBox(messages.disposalActionLabel(), actionList,
      s -> s.getActionCode() != null ? s.getActionCode().name() : "",
      (s, val) -> s.setActionCode(getDisposalActionCode(val)), true);

    // 5. Trigger Element (ListBox)
    ListBox triggerList = new ListBox();
    triggerList.addItem("", "");
    for (Pair<String, String> p : DisposalScheduleUtils.getElementsFromConfig()) {
      triggerList.addItem(p.getSecond(), p.getFirst());
    }
    FlowPanel triggerRow = form.addListBox(messages.disposalScheduleRetentionTriggerElementId(), triggerList,
      DisposalSchedule::getRetentionTriggerElementId, DisposalSchedule::setRetentionTriggerElementId, true);

    // 6. Interval (ListBox)
    ListBox intervalList = new ListBox();
    intervalList.addItem("", "");
    for (RetentionPeriodIntervalCode code : RetentionPeriodIntervalCode.values()) {
      intervalList.addItem(messages.disposalScheduleRetentionPeriodIntervalValue(code.name()), code.name());
    }
    FlowPanel intervalRow = form.addListBox(messages.disposalScheduleRetentionPeriodInterval(), intervalList,
      s -> s.getRetentionPeriodIntervalCode() != null ? s.getRetentionPeriodIntervalCode().name() : "",
      (s, val) -> s.setRetentionPeriodIntervalCode(getRetentionPeriodIntervalCode(val)), true);

    // 7. Duration (TextBox with Number Validation)
    TextBox durationBox = new TextBox();
    FlowPanel durationRow = form.addTextBoxBase(messages.disposalScheduleRetentionPeriodDuration(), durationBox,
      s -> s.getRetentionPeriodDuration() != null ? String.valueOf(s.getRetentionPeriodDuration()) : "", (s, val) -> {
        if (val != null && !val.isEmpty())
          s.setRetentionPeriodDuration(Integer.parseInt(val));
        else
          s.setRetentionPeriodDuration(null);
      }, true, false, "^[1-9]\\d*$", messages.numberIsRequired());

    // --- DEPENDENCY VISIBILITY LOGIC ---

    // Initial state
    actionRow.setVisible(!editMode);
    triggerRow.setVisible(false);
    intervalRow.setVisible(false);
    durationRow.setVisible(false);

    actionList.addChangeHandler(event -> {
      String val = actionList.getSelectedValue();
      if (val.isEmpty() || DisposalActionCode.RETAIN_PERMANENTLY.name().equals(val)) {
        triggerRow.setVisible(false);
        triggerList.setSelectedIndex(0);

        intervalRow.setVisible(false);
        intervalList.setSelectedIndex(0);

        durationRow.setVisible(false);
        durationBox.setText("");
      } else {
        triggerRow.setVisible(true);
      }
    });

    triggerList.addChangeHandler(event -> {
      boolean show = !triggerList.getSelectedValue().isEmpty();
      intervalRow.setVisible(show);
      intervalList.setSelectedIndex(0);

      durationRow.setVisible(show);
      durationBox.setText("");
    });

    intervalList.addChangeHandler(event -> {
      if (RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.name().equals(intervalList.getSelectedValue())) {
        durationBox.setEnabled(false);
        durationBox.setText("");
        durationBox.addStyleName("wui-cursor-not-allowed");
      } else {
        durationBox.setEnabled(true);
        durationBox.removeStyleName("wui-cursor-not-allowed");
      }
    });

    if (disposalSchedule != null) {
      form.setModel(disposalSchedule);

      // Manually trigger the visibility evaluation if data exists
      if (!editMode && disposalSchedule.getActionCode() != null) {
        boolean notPermanent = disposalSchedule.getActionCode() != DisposalActionCode.RETAIN_PERMANENTLY;
        triggerRow.setVisible(notPermanent);
        intervalRow.setVisible(notPermanent && triggerList.getSelectedIndex() > 0);
        durationRow.setVisible(notPermanent && intervalList.getSelectedIndex() > 0);

        if (disposalSchedule.getRetentionPeriodIntervalCode() == RetentionPeriodIntervalCode.NO_RETENTION_PERIOD) {
          durationBox.setEnabled(false);
          durationBox.addStyleName("wui-cursor-not-allowed");
        }
      }
    }
  }

  private DisposalActionCode getDisposalActionCode(String string) {
    if (string == null || string.isEmpty())
      return null;
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
    if (string == null || string.isEmpty())
      return null;
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
   * Defines what happens when the Save button is clicked. It automatically
   * validates the form before executing the runnable.
   */
  public void setSaveHandler(Runnable onSave) {
    saveButton.addClickHandler(event -> {
      if (isValid()) {
        onSave.run();
      }
    });
  }

  /**
   * Defines what happens when the Cancel button is clicked.
   */
  public void setCancelHandler(Runnable onCancel) {
    cancelButton.addClickHandler(event -> onCancel.run());
  }

  @Override
  public DisposalSchedule getValue() {
    return form.getValue();
  }

  @Override
  public boolean isValid() {
    return form.isValid();
  }

  public boolean isChanged() {
    return form.isChanged();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalSchedule> handler) {
    return form.addValueChangeHandler(handler);
  }
}