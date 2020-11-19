package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;

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

  // condition type

  @UiField
  Label conditionTypeListLabel;

  @UiField
  ListBox conditionTypeList;

  @UiField
  Label conditionTypeListError;

  @UiField(provided = true)
  MetadataFieldsPanel metadataFieldsPanel;

  @UiField(provided = true)
  ChildOfPanel childOfPanel;

  private final DisposalRule disposalRule;
  private final DisposalSchedules disposalSchedules;
  private final boolean editMode;

  private int selectedScheduleIndex;
  private int selectedTypeIndex;

  private boolean changed = false;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editMode) {
    metadataFieldsPanel = new MetadataFieldsPanel(disposalRule.getConditionKey(), disposalRule.getConditionValue(),
      editMode, disposalRule);
    childOfPanel = new ChildOfPanel(disposalRule.getConditionKey(), disposalRule.getConditionValue(), editMode,
      disposalRule);
    initWidget(uiBinder.createAndBindUi(this));

    errors.setVisible(false);
    this.editMode = editMode;
    this.disposalRule = disposalRule;
    this.disposalSchedules = disposalSchedules;

    metadataFieldsPanel.setVisible(false);
    childOfPanel.setVisible(false);

    initDisposalSchedulesList();
    initConditionTypeList();

    initHandlers();

    if (editMode) {
      setEditMode();
    }

  }

  private void setEditMode() {
    errors.setVisible(false);
    this.title.setText(disposalRule.getTitle());
    this.description.setText(disposalRule.getDescription());
    this.disposalSchedulesList.setSelectedIndex(selectedScheduleIndex);
    this.conditionTypeList.setSelectedIndex(selectedTypeIndex);

    if (disposalRule.getType().equals(ConditionType.METADATA_FIELD)) {
      metadataFieldsPanel.setVisible(true);
    } else {
      childOfPanel.setVisible(true);
    }
  }

  private void initConditionTypeList() {
    conditionTypeListLabel.setText(messages.disposalRuleType());
    List<ConditionType> conditionTypes = Arrays.asList(ConditionType.values());
    conditionTypeList.addItem("", "");
    if (!editMode) {
      for (ConditionType ruleType : conditionTypes) {
        conditionTypeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
      }
    } else {
      int index = 1;
      for (ConditionType ruleType : conditionTypes) {
        conditionTypeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
        if (ruleType.equals(disposalRule.getType())) {
          selectedTypeIndex = index;
        }
        index++;
      }
    }
  }

  private void initDisposalSchedulesList() {
    disposalSchedulesListLabel.setText(messages.disposalRuleScheduleName());
    disposalSchedulesList.addItem("", "");
    if (!editMode) {
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        if (schedule.getState().equals(DisposalScheduleState.ACTIVE)) {
          disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId());
        }
      }
    } else {
      int index = 1;
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId());
        if (schedule.getId().equals(disposalRule.getDisposalScheduleId())) {
          selectedScheduleIndex = index;
        }
        index++;
      }
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = event -> DisposalRuleDataPanel.this.onChange();

    ChangeHandler typeListChangeHandler = event -> {
      if (conditionTypeList.getSelectedValue().equals(ConditionType.IS_CHILD_OF.toString())) {
        metadataFieldsPanel.setVisible(false);
        childOfPanel.setVisible(true);
      } else if (conditionTypeList.getSelectedValue().equals(ConditionType.METADATA_FIELD.toString())) {
        metadataFieldsPanel.setVisible(true);
        childOfPanel.setVisible(false);
      } else {
        metadataFieldsPanel.setVisible(false);
        childOfPanel.setVisible(false);
      }
    };

    ValueChangeHandler valueChangeHandler = valueChangeEvent -> DisposalRuleDataPanel.this.onChange();

    title.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);

    disposalSchedulesList.addChangeHandler(changeHandler);

    conditionTypeList.addChangeHandler(changeHandler);
    conditionTypeList.addChangeHandler(typeListChangeHandler);

    metadataFieldsPanel.addValueChangeHandler(valueChangeHandler);
    childOfPanel.addValueChangeHandler(valueChangeHandler);

  }

  public DisposalRule getDisposalRule() {
    DisposalRule disposalRule = new DisposalRule();
    disposalRule.setTitle(title.getText());
    disposalRule.setDescription(description.getText());
    disposalRule.setDisposalScheduleId(disposalSchedulesList.getSelectedValue());
    disposalRule.setDisposalScheduleName(disposalSchedulesList.getSelectedItemText());
    if (conditionTypeList.getSelectedValue().equals(ConditionType.METADATA_FIELD.name())) {
      disposalRule.setType(ConditionType.METADATA_FIELD);
      disposalRule.setConditionKey(metadataFieldsPanel.getValue().getFirst());
      disposalRule.setConditionValue(metadataFieldsPanel.getValue().getSecond());
    } else {
      disposalRule.setType(ConditionType.IS_CHILD_OF);
      GWT.log("AQUI: " + childOfPanel.getValue());
      if (childOfPanel.getValue() != null) {
        disposalRule.setConditionKey(childOfPanel.getValue().getFirst());
        disposalRule.setConditionValue(childOfPanel.getValue().getSecond());
      }
    }
    return disposalRule;
  }

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

    if (conditionTypeList.getSelectedValue().length() == 0) {
      conditionTypeList.addStyleName("isWrong");
      conditionTypeListError.setText(messages.mandatoryField());
      conditionTypeListError.setVisible(true);
      Window.scrollTo(conditionTypeList.getAbsoluteLeft(), conditionTypeList.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleType()));
    } else {
      conditionTypeList.removeStyleName("isWrong");
      conditionTypeListError.setVisible(false);

      if (conditionTypeList.getSelectedValue().equals(ConditionType.METADATA_FIELD) && !metadataFieldsPanel.isValid()) {
        errorList.add(messages.isAMandatoryField(messages.disposalRuleCondition()));
      }

      if (conditionTypeList.getSelectedValue().equals(ConditionType.IS_CHILD_OF) && !childOfPanel.isValid()) {
        errorList.add(messages.isAMandatoryField(messages.disposalRuleCondition()));
      }
    }

    checked = true;
    return errorList.isEmpty();
  }

  public boolean isEditMode() {
    return editMode;
  }

  public boolean isChanged() {
    boolean conditionChanged = false;

    if (conditionTypeList.getSelectedValue().equals(ConditionType.METADATA_FIELD.name())) {
      conditionChanged = metadataFieldsPanel.isChanged();
    }

    if (conditionTypeList.getSelectedValue().equals(ConditionType.IS_CHILD_OF.name())) {
      conditionChanged = childOfPanel.isChanged();
    }

    return changed || conditionChanged;
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
