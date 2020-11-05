package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import org.roda.core.data.v2.ip.disposal.DisposalHold;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalRuleDataPanel extends Composite implements HasValueChangeHandlers<DisposalRule> {

  interface MyUiBinder extends UiBinder<Widget, DisposalRuleDataPanel> {
  }

  private static DisposalRuleDataPanel.MyUiBinder uiBinder = GWT.create(DisposalRuleDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox title;

  @UiField
  Label titleError;

  @UiField
  TextArea description;

  // disposal schedules list

  @UiField
  Label disposalSchedulesListLabel;

  @UiField
  ListBox disposalSchedulesList;

  @UiField
  Label disposalSchedulesListError;

  // disposal rule key

  @UiField
  Label keyLabel;

  @UiField
  TextBox key;

  @UiField
  Label keyError;

  // disposal rule value

  @UiField
  Label valueLabel;

  @UiField
  TextBox value;

  @UiField
  Label valueError;

  private DisposalRule disposalRule;
  private boolean editmode;
  private int selectedScheduleIndex;

  private boolean changed = false;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editmode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    this.disposalRule = disposalRule;
    errors.setVisible(false);

    init(disposalSchedules);

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        DisposalRuleDataPanel.this.onChange();
      }
    };
    
    title.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);
    key.addChangeHandler(changeHandler);
    value.addChangeHandler(changeHandler);
    disposalSchedulesList.addChangeHandler(changeHandler);

    if (editmode) {
      setDisposalRule(disposalRule);
    }
  }

  private void init(DisposalSchedules disposalSchedules) {
    disposalSchedulesListLabel.setText(messages.disposalRuleScheduleName());
    initDisposalSchedulesList(disposalSchedules);

    keyLabel.setText(messages.disposalRuleKey());
    valueLabel.setText(messages.disposalRuleValue());
  }

  private void initDisposalSchedulesList(DisposalSchedules disposalSchedules) {
    disposalSchedulesList.addItem("", "");
    if(!editmode){
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId());
      }
    }else{
      int index = 1;
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId());
        if(schedule.getId().equals(disposalRule.getDisposalScheduleId())){
          selectedScheduleIndex = index;
        }
        index++;
      }
    }
  }

  public void setDisposalRule(DisposalRule disposalRule) {
    this.title.setText(disposalRule.getTitle());
    this.description.setText(disposalRule.getDescription());
    this.disposalSchedulesList.setSelectedIndex(selectedScheduleIndex);
    this.key.setText(disposalRule.getKey());
    this.value.setText(disposalRule.getValue());
  }

  public DisposalRule getDisposalRule() {
    DisposalRule disposalRule = new DisposalRule();
    disposalRule.setTitle(title.getText());
    disposalRule.setDescription(description.getText());
    disposalRule.setDisposalScheduleId(disposalSchedulesList.getSelectedValue());
    disposalRule.setDisposalScheduleName(disposalSchedulesList.getSelectedItemText());
    disposalRule.setKey(key.getText());
    disposalRule.setValue(value.getText());
    return disposalRule;
  }

  /**
   * Is disposal rule data panel valid
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
      errorList.add(messages.isAMandatoryField(messages.disposalRuleTitle()));
    } else {
      title.removeStyleName("isWrong");
      titleError.setVisible(false);
    }

    if (disposalSchedulesList.getSelectedValue().length() == 0) {
      disposalSchedulesList.addStyleName("isWrong");
      disposalSchedulesListError.setText(messages.mandatoryField());
      disposalSchedulesListError.setVisible(true);
      Window.scrollTo(disposalSchedulesList.getAbsoluteLeft(), disposalSchedulesList.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleScheduleName()));
    } else {
      disposalSchedulesList.removeStyleName("isWrong");
      disposalSchedulesListError.setVisible(false);
    }

    if (key.getText().length() == 0) {
      key.addStyleName("isWrong");
      keyError.setText(messages.mandatoryField());
      keyError.setVisible(true);
      Window.scrollTo(key.getAbsoluteLeft(), key.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleKey()));
    } else {
      key.removeStyleName("isWrong");
      keyError.setVisible(false);
    }

    if (value.getText().length() == 0) {
      value.addStyleName("isWrong");
      valueError.setText(messages.mandatoryField());
      valueError.setVisible(true);
      Window.scrollTo(value.getAbsoluteLeft(), value.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleValue()));
    } else {
      value.removeStyleName("isWrong");
      valueError.setVisible(false);
    }

    checked = true;

    return errorList.isEmpty() ? true : false;
  }

  public void clear() {
    title.setText("");
    description.setText("");
  }

  public boolean isEditmode() {
    return editmode;
  }

  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalRule> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public DisposalRule getValue() {
    return getDisposalRule();
  }

}
