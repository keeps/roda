package org.roda.wui.client.disposal.rule.data.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalRuleDataPanel extends Composite
  implements GenericDataPanel<DisposalRule>, HasValueChangeHandlers<DisposalRule> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<DisposalRule> form;
  private final boolean editMode;
  private final Button saveButton;
  private final Button cancelButton;

  // --- Child Of Sub-Panel Variables ---
  private FlowPanel childOfRow;
  private TextBox childOfTextBox;
  private String childOfAipId;
  private String childOfAipTitle;

  private SelectionMethod selectionMethod = SelectionMethod.NONE;

  public DisposalRuleDataPanel(DisposalRule disposalRule, DisposalSchedules disposalSchedules, boolean editMode) {
    this.editMode = editMode;
    this.form = new GenericDataForm<>();

    // 1. Title
    form.addTextField(messages.disposalRuleTitle(), DisposalRule::getTitle, DisposalRule::setTitle, true, false);

    // 2. Description
    form.addTextArea(messages.disposalRuleDescription(), DisposalRule::getDescription, DisposalRule::setDescription,
      false, false);

    // 3. Disposal Schedules List
    ListBox disposalSchedulesList = new ListBox();
    disposalSchedules.getObjects().stream().filter(p -> !p.getState().equals(DisposalScheduleState.INACTIVE))
      .forEach(schedule -> disposalSchedulesList.addItem(schedule.getTitle(), schedule.getId()));

    form.addListBox(messages.disposalRuleScheduleName(), disposalSchedulesList, DisposalRule::getDisposalScheduleId,
      (r, val) -> {
        r.setDisposalScheduleId(val);
        if (disposalSchedulesList.getSelectedIndex() >= 0) {
          r.setDisposalScheduleName(disposalSchedulesList.getSelectedItemText());
        } else {
          r.setDisposalScheduleName(null);
        }
      }, true);

    // 4. Condition Type
    ListBox conditionTypeList = new ListBox();
    for (ConditionType ruleType : ConditionType.values()) {
      conditionTypeList.addItem(messages.disposalRuleTypeValue(ruleType.toString()), ruleType.toString());
    }

    form.addListBox(messages.disposalRuleType(), conditionTypeList, r -> r.getType() != null ? r.getType().name() : "",
      (r, val) -> r.setType(StringUtils.isNotBlank(val) ? ConditionType.valueOf(val) : null), true);

    // 5. Metadata Field Condition
    ListBox metadataFieldList = new ListBox();
    for (Pair<String, String> p : getElementsFromConfig()) {
      metadataFieldList.addItem(p.getSecond(), p.getFirst());
    }
    FlowPanel metadataFieldRow = form.addListBox(messages.disposalRuleCondition(), metadataFieldList,
      DisposalRule::getConditionKey, DisposalRule::setConditionKey, true);

    // 6. Metadata Value Condition
    FlowPanel metadataValueRow = form.addTextField(messages.disposalRuleConditionOperator(),
      DisposalRule::getConditionValue, DisposalRule::setConditionValue, true, false);

    // 7. IS_CHILD_OF Panel (Custom generic-form-field layout)
    initChildOfRow();
    form.addCustomWidget(childOfRow);

    // --- DEPENDENCY VISIBILITY LOGIC ---

    Runnable evaluateVisibility = () -> {
      String val = conditionTypeList.getSelectedValue();
      if (ConditionType.IS_CHILD_OF.name().equals(val)) {
        selectionMethod = SelectionMethod.CHILD_OF;
        metadataFieldRow.setVisible(false);
        metadataValueRow.setVisible(false);
        childOfRow.setVisible(true);
      } else if (ConditionType.METADATA_FIELD.name().equals(val)) {
        selectionMethod = SelectionMethod.METADATA_FIELD;
        metadataFieldRow.setVisible(true);
        metadataValueRow.setVisible(true);
        childOfRow.setVisible(false);
      } else {
        selectionMethod = SelectionMethod.NONE;
        metadataFieldRow.setVisible(false);
        metadataValueRow.setVisible(false);
        childOfRow.setVisible(false);
      }
    };

    conditionTypeList.addChangeHandler(event -> evaluateVisibility.run());

    // --- INITIALIZATION ---
    if (disposalRule != null) {
      form.setModel(disposalRule);

      // Initialize ChildOf variables if in edit mode
      if (editMode && ConditionType.IS_CHILD_OF.equals(disposalRule.getType())) {
        this.childOfAipId = disposalRule.getConditionKey();
        this.childOfAipTitle = disposalRule.getConditionValue();
        this.childOfTextBox.setText(childOfAipTitle + " (" + childOfAipId + ")");
      }

      if (conditionTypeList.getItemCount() > 0) {
        evaluateVisibility.run();
      }
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

  // --- CHILD OF CUSTOM ROW ---

  private void initChildOfRow() {
    childOfRow = new FlowPanel();
    childOfRow.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label childOfLabel = new Label(messages.selectParentTitle() + "*");
    childOfLabel.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    // Read-only textbox
    childOfTextBox = new TextBox();
    childOfTextBox.addStyleName("form-textbox");
    childOfTextBox.setReadOnly(true);

    // Button to open dialog
    Button selectAipBtn = new Button(messages.selectButton());
    selectAipBtn.addStyleName("btn btn-primary ma-pageview");
    selectAipBtn.addClickHandler(e -> {

      SelectAipDialog selectAipDialog = new SelectAipDialog(messages.selectParentTitle(),
        new Filter(new AllFilterParameter()), true, false);

      selectAipDialog.setSingleSelectionMode();

      selectAipDialog.addValueChangeHandler(evt -> {
        this.childOfAipId = evt.getValue().getId();
        this.childOfAipTitle = evt.getValue().getTitle();
        this.childOfTextBox.setText(childOfAipTitle + " (" + childOfAipId + ")");
        this.childOfTextBox.removeStyleName("isWrong");
        onChange();
      });
    });

    // Clear Button (Optional, but good UX to allow them to remove selection)
    Button clearBtn = new Button();
    clearBtn.setHTML("<i class='fa fa-trash'></i>");
    clearBtn.addStyleName("btn btn-danger btn-slim");
    clearBtn.addClickHandler(e -> {
      this.childOfAipId = null;
      this.childOfAipTitle = null;
      this.childOfTextBox.setText("");
      onChange();
    });

    inputPanel.add(childOfTextBox);
    inputPanel.add(selectAipBtn);
    inputPanel.add(clearBtn);

    leftPanel.add(childOfLabel);
    leftPanel.add(inputPanel);
    childOfRow.add(leftPanel);
  }

  // --- MIGRATED CONFIG METHODS ---

  private List<Pair<String, String>> getElementsFromConfig() {
    List<Pair<String, String>> elements = new ArrayList<>();
    String classSimpleName = IndexedAIP.class.getSimpleName();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.SEARCH_FIELD_PREFIX, classSimpleName);

    for (String field : fields) {
      String fieldPrefix = RodaConstants.SEARCH_FIELD_PREFIX + '.' + classSimpleName + '.' + field;
      String fieldType = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldsName = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_FIELDS);

      if (RodaConstants.SEARCH_FIELD_TYPE_TEXT.equals(fieldType) && showField(field)) {
        String fieldLabelI18N = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N);
        String translation = fieldLabelI18N;
        try {
          translation = ConfigurationManager.getTranslation(fieldLabelI18N);
        } catch (MissingResourceException e) {
          // do nothing
        }

        Pair<String, String> pair = new Pair<>(fieldsName, translation);
        elements.add(pair);
      }
    }
    return elements;
  }

  private boolean showField(String fieldsName) {
    List<String> blackList = ConfigurationManager.getStringList(RodaConstants.DISPOSAL_RULE_BLACKLIST_CONDITION);
    return !blackList.contains(fieldsName);
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

  // --- GENERIC DATA PANEL OVERRIDES ---

  @Override
  public DisposalRule getValue() {
    DisposalRule rule = form.getValue();

    // Inject custom sub-panel values back into the model before returning
    if (rule.getType() == ConditionType.IS_CHILD_OF) {
      rule.setConditionKey(childOfAipId);
      rule.setConditionValue(childOfAipTitle);
    }

    return rule;
  }

  @Override
  public boolean isValid() {
    boolean valid = form.isValid();
    List<String> extraErrors = new ArrayList<>();

    // Manually validate the custom ChildOf row
    if (selectionMethod == SelectionMethod.CHILD_OF) {
      if (StringUtils.isBlank(childOfAipId)) {
        valid = false;
        childOfTextBox.addStyleName("isWrong");
        extraErrors.add(messages.isAMandatoryField(messages.selectParentTitle()));
      } else {
        childOfTextBox.removeStyleName("isWrong");
      }
    }

    // Append extra errors to the GenericDataForm's native error block
    if (!extraErrors.isEmpty()) {
      String currentErrorsHTML = form.getErrors().getHTML() != null ? form.getErrors().getHTML() : "";
      StringBuilder sb = new StringBuilder(currentErrorsHTML);
      for (String err : extraErrors) {
        sb.append("<span class='error'>").append(err).append("</span><br/>");
      }
      form.getErrors().setHTML(sb.toString());
      form.getErrors().setVisible(true);
    }

    return valid;
  }

  public boolean isEditMode() {
    return editMode;
  }

  public boolean isChanged() {
    boolean conditionChanged = false;

    if (selectionMethod == SelectionMethod.CHILD_OF) {
      // Check if the current ID differs from the original model's ID
      String originalId = null;
      if (form.getValue() != null && ConditionType.IS_CHILD_OF.equals(form.getValue().getType())) {
        originalId = form.getValue().getConditionKey();
      }
      if (childOfAipId != null && !childOfAipId.equals(originalId)) {
        conditionChanged = true;
      } else if (childOfAipId == null && originalId != null) {
        conditionChanged = true;
      }
    }

    return form.isChanged() || conditionChanged;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalRule> handler) {
    return form.addValueChangeHandler(handler);
  }

  protected void onChange() {
    if (form.isChanged()) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  private enum SelectionMethod {
    CHILD_OF, METADATA_FIELD, NONE
  }
}