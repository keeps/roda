/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class MetadataFieldsPanel extends Composite implements HasValueChangeHandlers<Pair<String, String>> {

  public static final String IS_WRONG = "isWrong";

  interface MyUiBinder extends UiBinder<Widget, MetadataFieldsPanel> {
  }

  private static MetadataFieldsPanel.MyUiBinder uiBinder = GWT.create(MetadataFieldsPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // METADATA_FIELD

  @UiField
  Label metadataFieldLabel;

  @UiField
  Label metadataFieldError;

  @UiField
  ListBox fieldsList;

  @UiField
  Label fieldsOperator;

  @UiField
  TextBox fieldValue;

  private final boolean editMode;

  private String conditionKey;

  private int selectedConditionIndex;

  private boolean changed = false;
  private boolean checked = false;

  public MetadataFieldsPanel(String conditionKey, String conditionValue, boolean editMode, DisposalRule disposalRule) {
    initWidget(uiBinder.createAndBindUi(this));

    fieldsOperator.setText(messages.disposalRuleConditionOperator());

    this.editMode = editMode;
    this.conditionKey = conditionKey;

    initList();
    initHandler();

    if (editMode && disposalRule.getType().equals(ConditionType.METADATA_FIELD)) {
      fieldsList.setSelectedIndex(selectedConditionIndex);
      fieldValue.setText(conditionValue);
    }
  }

  private void initHandler() {
    ChangeHandler changeHandler = event -> MetadataFieldsPanel.this.onChange();

    KeyUpHandler keyUpHandler = event -> MetadataFieldsPanel.this.onChange();

    fieldsList.addChangeHandler(changeHandler);
    fieldValue.addChangeHandler(changeHandler);
    fieldValue.addKeyUpHandler(keyUpHandler);
  }

  private static List<Pair<String, String>> getElementsFromConfig() {
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

  private static boolean showField(String fieldsName) {
    List<String> blackList = ConfigurationManager.getStringList(RodaConstants.DISPOSAL_RULE_BLACKLIST_CONDITION);
    return !blackList.contains(fieldsName);
  }

  private void initList() {
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
        if (value.getFirst().equals(conditionKey)) {
          selectedConditionIndex = index;
        }
        index++;
      }
    }
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    if (fieldsList.getSelectedIndex() == 0) {
      fieldsList.addStyleName(IS_WRONG);
      fieldValue.addStyleName(IS_WRONG);
      metadataFieldError.setText(messages.mandatoryField());
      metadataFieldError.setVisible(true);
      Window.scrollTo(fieldsList.getAbsoluteLeft(), fieldsList.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalRuleCondition()));
    } else {
      if (StringUtils.isBlank(fieldValue.getText())) {
        fieldsList.removeStyleName(IS_WRONG);
        fieldValue.addStyleName(IS_WRONG);
        metadataFieldError.setText(messages.mandatoryField());
        metadataFieldError.setVisible(true);
        Window.scrollTo(fieldValue.getAbsoluteLeft(), fieldValue.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField(messages.disposalRuleCondition()));
      } else {
        fieldsList.removeStyleName(IS_WRONG);
        fieldValue.removeStyleName(IS_WRONG);
        metadataFieldError.setVisible(false);
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
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Pair<String, String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Pair<String, String> getValue() {
    return getMetadataFields();
  }

  private Pair<String, String> getMetadataFields() {
    Pair<String, String> metadataFields = new Pair<>();
    metadataFields.setFirst(fieldsList.getSelectedValue());
    metadataFields.setSecond(fieldValue.getText());
    return metadataFields;
  }

}
