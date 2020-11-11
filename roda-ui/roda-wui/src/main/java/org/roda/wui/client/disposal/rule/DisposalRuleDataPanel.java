package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.wui.client.ingest.process.PluginParameterPanel;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

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
  Label typeListLabel;

  @UiField
  ListBox typeList;

  @UiField
  Label typeListError;

  // condition

  @UiField
  Label conditionLabel;

  @UiField
  FlowPanel conditionPanel;

  @UiField
  Label conditionError;

  @UiField
  ListBox fieldsList;

  @UiField
  Label fieldsOperator;

  @UiField
  TextBox fieldValue;

  // IS_CHILD_OF

  @UiField(provided = true)
  PluginParameterPanel pluginParameterPanel;

  private HorizontalPanel childOfPanel = new HorizontalPanel();

  private DisposalRule disposalRule;
  private DisposalSchedules disposalSchedules;
  private boolean editmode;

  ConditionType conditionType = null;
  private String conditionKey;
  private String conditionValue;

  private int selectedScheduleIndex;
  private int selectedTypeIndex;
  private int selectedConditionIndex;

  private boolean changed = false;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editmode) {
    initPluginParameterPanel();
    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    this.disposalRule = disposalRule;
    this.disposalSchedules = disposalSchedules;

    setInitialState();
    initDisposalSchedulesList();
    initTypeList();
    initConditionList();
    initHandlers();
    if (editmode) {
      initEditMode();
    }
  }

  private void initEditMode() {
    errors.setVisible(false);
    this.title.setText(disposalRule.getTitle());
    this.description.setText(disposalRule.getDescription());
    this.disposalSchedulesList.setSelectedIndex(selectedScheduleIndex);
    this.typeList.setSelectedIndex(selectedTypeIndex);

    if(disposalRule.getType().equals(ConditionType.METADATA_FIELD)){
      conditionLabel.setVisible(true);
      conditionPanel.setVisible(true);
      fieldsList.setSelectedIndex(selectedConditionIndex);
      fieldValue.setText(disposalRule.getConditionValue());
    }else{
      conditionLabel.setVisible(true);
      conditionLabel.setText(messages.conditionAtualParent());

      conditionPanel.setVisible(true);
      fieldsList.setVisible(false);
      fieldsOperator.setVisible(false);
      fieldValue.setVisible(false);

      Label childOfLabel = new Label();

      Anchor anchor = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
      anchor.addStyleName("toolbarLink toolbarLinkSmall");
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          conditionLabel.setVisible(false);
          conditionLabel.setText(messages.disposalRuleCondition());
          conditionPanel.setVisible(false);
          pluginParameterPanel.setVisible(true);
          childOfPanel.clear();
        }
      });

      String parent = disposalRule.getConditionValue() + " (" + disposalRule.getConditionKey() + ")";
      childOfLabel.setText(parent);
      childOfLabel.addStyleName("itemText value");

      childOfPanel.add(childOfLabel);
      childOfPanel.setCellWidth(childOfLabel, "100%");

      childOfPanel.add(anchor);

      conditionPanel.add(childOfPanel);
    }

  }

  private void setInitialState() {
    errors.setVisible(false);
    conditionLabel.setVisible(false);
    conditionPanel.setVisible(false);
    pluginParameterPanel.setVisible(false);
  }

  private void initPluginParameterPanel() {
    pluginParameterPanel = new PluginParameterPanel(new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      messages.selectParentTitle(), PluginParameter.PluginParameterType.AIP_ID, "", false, false,
      "Use the provided parent node if the SIPs does not provide one."));
    pluginParameterPanel.getLayout().removeStyleName("plugin-options-parameter");
  }

  private void initTypeList() {
    typeListLabel.setText(messages.disposalRuleType());
    List<ConditionType> conditionTypes = Arrays.asList(ConditionType.values());
    typeList.addItem("", "");
    if(!editmode) {
      for (ConditionType ruleType : conditionTypes) {
        typeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
      }
    }else{
      int index = 1;
      for (ConditionType ruleType : conditionTypes) {
        typeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
        if(ruleType.equals(disposalRule.getType())){
          selectedTypeIndex = index;
        }
        index++;
      }
    }
  }

  private void initDisposalSchedulesList() {
    disposalSchedulesListLabel.setText(messages.disposalRuleScheduleName());
    disposalSchedulesList.addItem("", "");
    if (!editmode) {
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        if(schedule.getState().equals(DisposalScheduleState.ACTIVE)) {
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

  private static Map<String, String> getConditionsFromConfig() {
    Map<String, String> conditions = new HashMap<>();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.DISPOSAL_RULE_CONDITION_PREFIX);
    for (String field : fields) {
      String fieldName = ConfigurationManager.getString(RodaConstants.DISPOSAL_RULE_CONDITION_PREFIX, field,
        RodaConstants.DISPOSAL_RULE_CONDITION_FIELD);
      String fieldType = ConfigurationManager.getString(RodaConstants.DISPOSAL_RULE_CONDITION_PREFIX, field,
        RodaConstants.DISPOSAL_RULE_CONDITION_TYPE);
      String fieldLabelI18N = ConfigurationManager.getString(RodaConstants.DISPOSAL_RULE_CONDITION_PREFIX, field,
        RodaConstants.DISPOSAL_RULE_CONDITION_I18N);

      if (fieldName != null && fieldType != null && fieldLabelI18N != null) {
        conditions.put(fieldName, fieldLabelI18N);
      }
    }
    return conditions;
  }

  private void initConditionList() {
    Map<String, String> list = getConditionsFromConfig();
    fieldsList.addItem("", "");
    if(!editmode) {
      for (Map.Entry<String, String> entry : list.entrySet()) {
        fieldsList.addItem(entry.getKey(), entry.getKey());
      }
    }else{
      int index = 1;
      for (Map.Entry<String, String> entry : list.entrySet()) {
        fieldsList.addItem(entry.getKey(), entry.getKey());
        if(entry.getKey().equals(disposalRule.getConditionKey())){
          selectedConditionIndex = index;
        }
        index++;
      }
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        DisposalRuleDataPanel.this.onChange();
      }
    };

    title.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);
    disposalSchedulesList.addChangeHandler(changeHandler);
    typeList.addChangeHandler(changeHandler);
    fieldsList.addChangeHandler(changeHandler);
    fieldValue.addChangeHandler(changeHandler);

    ChangeHandler typeListChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (typeList.getSelectedValue().equals(ConditionType.IS_CHILD_OF.toString())) {
          conditionLabel.setVisible(false);
          pluginParameterPanel.setVisible(true);
          conditionPanel.setVisible(false);
        } else if (typeList.getSelectedValue().equals(ConditionType.METADATA_FIELD.toString())) {
          childOfPanel.setVisible(false);
          conditionLabel.setVisible(true);
          pluginParameterPanel.setVisible(false);
          conditionPanel.setVisible(true);
          fieldsList.setVisible(true);
          fieldsOperator.setVisible(true);
          fieldValue.setVisible(true);
        } else {
          childOfPanel.setVisible(false);
          conditionLabel.setVisible(false);
          pluginParameterPanel.setVisible(false);
          conditionPanel.setVisible(false);
        }
      }
    };

    typeList.addChangeHandler(typeListChangeHandler);

  }

  public void setDisposalRule(DisposalRule disposalRule) {

  }

  public DisposalRule getDisposalRule() {
    DisposalRule disposalRule = new DisposalRule();
    disposalRule.setTitle(title.getText());
    disposalRule.setDescription(description.getText());
    disposalRule.setDisposalScheduleId(disposalSchedulesList.getSelectedValue());
    disposalRule.setDisposalScheduleName(disposalSchedulesList.getSelectedItemText());
    if (typeList.getSelectedValue().equals(ConditionType.IS_CHILD_OF.toString())) {
      disposalRule.setType(ConditionType.IS_CHILD_OF);
    } else {
      disposalRule.setType(ConditionType.METADATA_FIELD);
    }
    disposalRule.setConditionKey(conditionKey);
    disposalRule.setConditionValue(conditionValue);

    return disposalRule;
  }

  private boolean isConditionValid() {
    if (conditionType.equals(ConditionType.METADATA_FIELD)) {
      conditionKey = fieldsList.getSelectedValue();
      conditionValue = fieldValue.getValue();
    } else if (conditionType.equals(ConditionType.IS_CHILD_OF)) {
      conditionKey = pluginParameterPanel.getValue();
      conditionValue = pluginParameterPanel.getAipTitle();
    }
    return StringUtils.isNotBlank(conditionKey) && StringUtils.isNotBlank(conditionValue);
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

    if (!editmode) {
      if (typeList.getSelectedValue().length() == 0) {
        typeList.addStyleName("isWrong");
        typeListError.setText(messages.mandatoryField());
        typeListError.setVisible(true);
        Window.scrollTo(typeList.getAbsoluteLeft(), typeList.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.disposalRuleType()));
      } else {
        conditionType = ConditionType.valueOf(typeList.getSelectedValue());
        typeList.removeStyleName("isWrong");
        typeListError.setVisible(false);

        if (!isConditionValid()) {
          GWT.log("entrei");
          if (conditionType.equals(ConditionType.IS_CHILD_OF)) {
            pluginParameterPanel.addStyleName("isWrong");
            Window.scrollTo(pluginParameterPanel.getAbsoluteLeft(), pluginParameterPanel.getAbsoluteTop());
          } else {
            GWT.log("panados");
            conditionPanel.addStyleName("isWrong");
            Window.scrollTo(conditionPanel.getAbsoluteLeft(), conditionPanel.getAbsoluteTop());
          }
          conditionError.setText(messages.mandatoryField());
          conditionError.setVisible(true);
          errorList.add(messages.isAMandatoryField(messages.disposalRuleType()));
        } else {
          if (conditionType.equals(ConditionType.IS_CHILD_OF)) {
            pluginParameterPanel.removeStyleName("isWrong");
          } else {
            conditionPanel.removeStyleName("isWrong");
          }
          conditionError.setVisible(false);
        }
      }
    }

    checked = true;

    return errorList.isEmpty() ? true : false;
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
