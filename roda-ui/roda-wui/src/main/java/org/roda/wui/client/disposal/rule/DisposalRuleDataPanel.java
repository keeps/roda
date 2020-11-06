package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRuleType;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchFieldPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.PluginParameterPanel;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
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

  // disposal rule type

  @UiField
  Label ruleTypeListLabel;

  @UiField
  ListBox ruleTypeList;

  @UiField
  Label ruleTypeListError;

  // IS_CHILD_OF

  @UiField(provided = true)
  PluginParameterPanel pluginParameterPanel;

  // METADATA_FIELD

  @UiField
  FlowPanel metadataFieldsPanel;

  @UiField
  FlowPanel metadataFieldsPanelButtons;

  @UiField
  Button metadataFieldsAddField;

  @UiField
  Button metadataFieldsClean;

  private AdvancedSearchFieldsPanel metadataFields;

  private DisposalRule disposalRule;
  private DisposalSchedules disposalSchedules;
  private boolean editmode;
  private int selectedScheduleIndex;

  DisposalRuleType disposalRuleType = null;
  private String isChildOfValue = null;
  private Map<String, FilterParameter> metadataFieldsValues = null;

  private boolean changed = false;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editmode) {
    initPluginParameterPanel();

    initWidget(uiBinder.createAndBindUi(this));
    initMetadataFieldPanel();

    this.editmode = editmode;
    this.disposalRule = disposalRule;
    this.disposalSchedules = disposalSchedules;

    initFields();
    initHandlers();
    setInitialState();

    if (editmode) {
      setDisposalRule(disposalRule);
    }
  }

  private void initMetadataFieldPanel() {
    metadataFields = new AdvancedSearchFieldsPanel(IndexedAIP.class.getSimpleName(), keyCode -> {
    });
    metadataFieldsPanel.insert(metadataFields, 0);
  }

  private void setInitialState() {
    errors.setVisible(false);
    pluginParameterPanel.setVisible(false);
    metadataFieldsPanel.setVisible(false);
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

    ChangeHandler ruleTypeListChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (ruleTypeList.getSelectedValue().equals(DisposalRuleType.IS_CHILD_OF.toString())) {
          pluginParameterPanel.setVisible(true);
          metadataFieldsPanel.setVisible(false);
        } else if (ruleTypeList.getSelectedValue().equals(DisposalRuleType.METADATA_FIELD.toString())) {
          pluginParameterPanel.setVisible(false);
          metadataFieldsPanel.setVisible(true);
        } else {
          pluginParameterPanel.setVisible(false);
          metadataFieldsPanel.setVisible(false);
        }
      }
    };

    ruleTypeList.addChangeHandler(ruleTypeListChangeHandler);

  }

  private void initPluginParameterPanel() {
    pluginParameterPanel = new PluginParameterPanel(new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      messages.selectParentTitle(), PluginParameter.PluginParameterType.AIP_ID, "", false, false,
      "Use the provided parent node if the SIPs does not provide one."));
    pluginParameterPanel.getLayout().removeStyleName("plugin-options-parameter");
  }

  private void initFields() {
    disposalSchedulesListLabel.setText(messages.disposalRuleScheduleName());
    initDisposalSchedulesList();

    ruleTypeListLabel.setText(messages.disposalRuleType());
    initTypeList();

  }

  private void initTypeList() {
    List<DisposalRuleType> disposalRuleTypes = Arrays.asList(DisposalRuleType.values());
    ruleTypeList.addItem("", "");
    for (DisposalRuleType ruleType : disposalRuleTypes) {
      ruleTypeList.addItem(ruleType.toString(), ruleType.toString());
    }
  }

  private void initDisposalSchedulesList() {
    disposalSchedulesList.addItem("", "");
    if (!editmode) {
      for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
        disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId());
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

  public void setDisposalRule(DisposalRule disposalRule) {
    this.title.setText(disposalRule.getTitle());
    this.description.setText(disposalRule.getDescription());
    this.disposalSchedulesList.setSelectedIndex(selectedScheduleIndex);
  }

  public DisposalRule getDisposalRule() {
    DisposalRule disposalRule = new DisposalRule();
    disposalRule.setTitle(title.getText());
    disposalRule.setDescription(description.getText());
    disposalRule.setDisposalScheduleId(disposalSchedulesList.getSelectedValue());
    disposalRule.setDisposalScheduleName(disposalSchedulesList.getSelectedItemText());
    if(disposalRuleType!= null ) {
      disposalRule.setType(disposalRuleType);
      if (disposalRuleType.equals(DisposalRuleType.IS_CHILD_OF)) {
        disposalRule.setIsChildOf(isChildOfValue);
      } else {
        disposalRule.setMetadataFields(metadataFieldsValues);
      }
    }
    return disposalRule;
  }

  private boolean isRuleTypeValueValid() {
    boolean ret = false;
    if (disposalRuleType.equals(DisposalRuleType.METADATA_FIELD)) {
      setMetadataFields();
      ret = metadataFieldsValues.size() > 0;
    } else if (disposalRuleType.equals(DisposalRuleType.IS_CHILD_OF)) {
      setIsChildOf();
      ret = StringUtils.isNotBlank(isChildOfValue);
    }
    return ret;
  }

  private void setIsChildOf() {
    isChildOfValue = pluginParameterPanel.getValue();
  }

  private void setMetadataFields() {
    metadataFieldsValues = new HashMap<>();
    if (this.metadataFieldsPanel.isVisible()) {
      for (int i = 0; i < metadataFields.getWidgetCount(); i++) {
        if (metadataFields.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) metadataFields.getWidget(i);
          String searchFieldId = searchAdvancedFieldPanel.getSearchField().getId();
          FilterParameter oldFilterParameter = metadataFieldsValues.get(searchFieldId);
          FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

          if (filterParameter instanceof SimpleFilterParameter) {
            SimpleFilterParameter parameter = (SimpleFilterParameter) filterParameter;
            if (RodaConstants.AIP_LEVEL.equals(parameter.getName())
              && RodaConstants.NONE_SELECTED_LEVEL.equals(parameter.getValue())) {
              filterParameter = null;
            }
          }

          if (filterParameter != null) {
            if (oldFilterParameter != null) {
              if (oldFilterParameter instanceof OrFiltersParameters) {
                List<FilterParameter> filterParameters = ((OrFiltersParameters) oldFilterParameter).getValues();
                filterParameters.add(filterParameter);
                ((OrFiltersParameters) oldFilterParameter).setValues(filterParameters);
                metadataFieldsValues.put(searchFieldId, oldFilterParameter);
              } else {
                List<FilterParameter> filterParameters = new ArrayList<>();
                filterParameters.add(oldFilterParameter);
                filterParameters.add(filterParameter);
                metadataFieldsValues.put(searchFieldId, new OrFiltersParameters(filterParameters));
              }
            } else {
              metadataFieldsValues.put(searchFieldId, filterParameter);
            }
          }
        }
      }
    }
  }

  @UiHandler("metadataFieldsAddField")
  void handleMetadataFieldsAdd(ClickEvent e) {
    metadataFields.addSearchFieldPanel();
  }

  @UiHandler("metadataFieldsClean")
  void handleMetadataFieldsClean(ClickEvent e) {
    JavascriptUtils.cleanAdvancedSearch();
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

    if (!isRuleTypeValid()) {
      ruleTypeList.addStyleName("isWrong");
      ruleTypeListError.setText(messages.mandatoryField());
      ruleTypeListError.setVisible(true);
      Window.scrollTo(ruleTypeList.getAbsoluteLeft(), ruleTypeList.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleType()));
    } else {
      ruleTypeList.removeStyleName("isWrong");
      ruleTypeListError.setVisible(false);
    }

    if(!isRuleTypeValueValid()){
      ruleTypeList.addStyleName("isWrong");
      ruleTypeListError.setText(messages.mandatoryField());
      ruleTypeListError.setVisible(true);
      Window.scrollTo(ruleTypeList.getAbsoluteLeft(), ruleTypeList.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleType()));
    }else {
      ruleTypeList.removeStyleName("isWrong");
      ruleTypeListError.setVisible(false);
    }

    checked = true;

    return errorList.isEmpty() ? true : false;
  }

  private boolean isRuleTypeValid() {
    if (ruleTypeList.getSelectedValue().equals(DisposalRuleType.IS_CHILD_OF.toString())) {
      disposalRuleType = DisposalRuleType.IS_CHILD_OF;
      return true;
    } else if (ruleTypeList.getSelectedValue().equals(DisposalRuleType.METADATA_FIELD.toString())) {
      disposalRuleType = DisposalRuleType.METADATA_FIELD;
      return true;
    }
    return false;
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
