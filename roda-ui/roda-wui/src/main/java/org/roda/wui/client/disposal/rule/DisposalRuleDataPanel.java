package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedAIP;
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
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

  private final HorizontalPanel childOfPanel = new HorizontalPanel();

  private final DisposalRule disposalRule;
  private final DisposalSchedules disposalSchedules;
  private final boolean editMode;

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

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editMode) {
    initPluginParameterPanel();
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;
    this.disposalRule = disposalRule;
    this.disposalSchedules = disposalSchedules;

    setInitialState();
    initDisposalSchedulesList();
    initTypeList();
    initConditionList();
    initHandlers();
    if (editMode) {
      initEditMode();
    }
  }

  private void initEditMode() {
    errors.setVisible(false);
    this.title.setText(disposalRule.getTitle());
    this.description.setText(disposalRule.getDescription());
    this.disposalSchedulesList.setSelectedIndex(selectedScheduleIndex);
    this.typeList.setSelectedIndex(selectedTypeIndex);

    if (disposalRule.getType().equals(ConditionType.METADATA_FIELD)) {
      conditionLabel.setVisible(true);
      conditionPanel.setVisible(true);
      fieldsList.setSelectedIndex(selectedConditionIndex);
      fieldValue.setText(disposalRule.getConditionValue());
    } else {
      conditionLabel.setVisible(true);
      conditionLabel.setText(messages.conditionActualParent());

      conditionPanel.setVisible(true);
      fieldsList.setVisible(false);
      fieldsOperator.setVisible(false);
      fieldValue.setVisible(false);

      Label childOfLabel = new Label();

      Anchor anchor = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
      anchor.addStyleName("toolbarLink toolbarLinkSmall");
      anchor.addClickHandler(clickEvent -> {
        conditionLabel.setVisible(false);
        conditionLabel.setText(messages.disposalRuleCondition());
        conditionPanel.setVisible(false);
        pluginParameterPanel.setVisible(true);
        childOfPanel.clear();
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
    if (!editMode) {
      for (ConditionType ruleType : conditionTypes) {
        typeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
      }
    } else {
      int index = 1;
      for (ConditionType ruleType : conditionTypes) {
        typeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
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

  private static List<Pair<String, String>> getElementsFromConfig() {
    List<Pair<String, String>> elements = new ArrayList<>();
    String classSimpleName = IndexedAIP.class.getSimpleName();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.SEARCH_FIELD_PREFIX, classSimpleName);

    for (String field : fields) {
      String fieldPrefix = RodaConstants.SEARCH_FIELD_PREFIX + '.' + classSimpleName + '.' + field;
      String fieldType = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldsName = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_FIELDS);

      if (RodaConstants.SEARCH_FIELD_TYPE_TEXT.equals(fieldType) && isWhiteList(field)) {
        String fieldLabelI18N = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N);
        String translation = fieldLabelI18N;
        try {
          translation = ConfigurationManager.getTranslation(fieldLabelI18N);
        } catch (MissingResourceException e) {
          // do nothing;
        }

        Pair<String, String> pair = new Pair<>(fieldsName, translation);
        elements.add(pair);
      }

    }
    return elements;
  }

  private static boolean isWhiteList(String fieldsName) {
    boolean ret = false;
    List<String> whitelistFields = ConfigurationManager.getStringList(RodaConstants.DISPOSAL_RULE_WHITELIST_CONDITION);
    for (String field : whitelistFields) {
      if (field.equals(fieldsName)) {
        ret = true;
      }
    }
    return ret;
  }

  private void initConditionList() {
    List<Pair<String, String>> conditionList = getElementsFromConfig();
    fieldsList.addItem("", "");
    if (!editMode) {
      for (Pair<String, String> value : conditionList) {
        fieldsList.addItem(value.getSecond(), value.getFirst());
      }
    } else {
      int index = 1;
      for (Pair<String, String> value : conditionList) {
        fieldsList.addItem(value.getSecond(), value.getFirst());
        if (value.getSecond().equals(disposalRule.getConditionKey())) {
          selectedConditionIndex = index;
        }
        index++;
      }
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = event -> DisposalRuleDataPanel.this.onChange();

    title.addChangeHandler(changeHandler);
    description.addChangeHandler(changeHandler);
    disposalSchedulesList.addChangeHandler(changeHandler);
    typeList.addChangeHandler(changeHandler);
    fieldsList.addChangeHandler(changeHandler);
    fieldValue.addChangeHandler(changeHandler);

    ChangeHandler typeListChangeHandler = event -> {
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
    };

    typeList.addChangeHandler(typeListChangeHandler);

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

    if (!editMode) {
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
          if (conditionType.equals(ConditionType.IS_CHILD_OF)) {
            pluginParameterPanel.addStyleName("isWrong");
            Window.scrollTo(pluginParameterPanel.getAbsoluteLeft(), pluginParameterPanel.getAbsoluteTop());
          } else {
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

    return errorList.isEmpty();
  }

  public boolean isEditMode() {
    return editMode;
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
