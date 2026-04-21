package org.roda.wui.client.common.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.search.SearchSuggestBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class GenericDataForm<T> extends Composite implements GenericDataPanel<T>, HasValueChangeHandlers<T> {

  private static GenericDataFormUiBinder uiBinder = GWT.create(GenericDataFormUiBinder.class);

  private final List<FormBinding<T>> bindings = new ArrayList<>();

  @UiField
  FlowPanel fieldsContainer;

  @UiField
  HTML errors;

  private T model;
  private boolean changed = false;

  public GenericDataForm() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setModel(T model) {
    this.model = model;
    this.changed = false;
    refreshUI();
  }

  @Override
  public T getValue() {
    for (FormBinding<T> binding : bindings) {
      binding.flushToModel(model);
    }
    return model;
  }

  @Override
  public boolean isValid() {
    List<String> errorMessages = new ArrayList<>();
    boolean valid = true;

    for (FormBinding<T> binding : bindings) {
      if (!binding.validate()) {
        valid = false;
        String errMsg = binding.getRegexErrorMessage() != null ? binding.getRegexErrorMessage()
          : "Field '" + binding.getLabelText() + "' is invalid or mandatory.";
        errorMessages.add(errMsg);
      }
    }

    if (!valid) {
      StringBuilder sb = new StringBuilder();
      for (String err : errorMessages) {
        sb.append("<span class='error'>").append(err).append("</span><br/>");
      }
      errors.setHTML(sb.toString());
      errors.setVisible(true);
    } else {
      errors.setVisible(false);
    }

    return valid;
  }

  public HTML getErrors() {
    return errors;
  }

  public boolean isChanged() {
    return changed;
  }

  public void addCustomWidget(Widget widget) {
    fieldsContainer.add(widget);
  }

  // --- READ ONLY ---

  public FlowPanel addReadOnlyField(String labelText, Function<T, String> getter, boolean mandatory) {
    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    label.addStyleName("form-label");
    if (mandatory) {
      label.addStyleName("form-label-mandatory");
    }

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    InlineHTML inlineHTML = new InlineHTML();
    inlineHTML.addStyleName("form-readonly-value");

    inputPanel.add(inlineHTML);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);

    fieldsContainer.add(searchField);
    bindings.add(new ReadOnlyFieldBinding(labelText, searchField, inlineHTML, getter));

    return searchField;
  }

  // --- TEXT FIELDS & TEXT AREAS ---

  public FlowPanel addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
    boolean mandatory) {
    return addTextBoxBase(labelText, new TextBox(), getter, setter, mandatory, false, null, null);
  }

  public FlowPanel addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
    boolean mandatory, boolean readOnly) {
    return addTextBoxBase(labelText, new TextBox(), getter, setter, mandatory, readOnly, null, null);
  }

  public FlowPanel addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
    boolean mandatory, boolean readOnly, String regex, String regexErrorMessage) {
    return addTextBoxBase(labelText, new TextBox(), getter, setter, mandatory, readOnly, regex, regexErrorMessage);
  }

  public FlowPanel addTextArea(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
    boolean mandatory) {
    return addTextArea(labelText, getter, setter, mandatory, false);
  }

  public FlowPanel addTextArea(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
    boolean mandatory, boolean readOnly) {
    TextArea textArea = new TextArea();
    textArea.addStyleName("metadata-form-text-area");
    return addTextBoxBase(labelText, textArea, getter, setter, mandatory, readOnly, null, null);
  }

  public FlowPanel addTextBoxBase(String labelText, TextBoxBase textBoxBase, Function<T, String> getter,
    BiConsumer<T, String> setter, boolean mandatory, boolean readOnly, String regex, String regexErrorMessage) {

    if (readOnly) {
      return addReadOnlyField(labelText, getter, mandatory);
    }

    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    label.addStyleName("form-label");
    if (mandatory) {
      label.addStyleName("form-label-mandatory");
    }

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    textBoxBase.addStyleName("form-textbox");

    inputPanel.add(textBoxBase);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);
    fieldsContainer.add(searchField);

    ChangeHandler changeHandler = event -> onChange();
    KeyUpHandler keyUpHandler = event -> onChange();
    textBoxBase.addChangeHandler(changeHandler);
    textBoxBase.addKeyUpHandler(keyUpHandler);

    bindings.add(
      new TextBoxBaseBinding(labelText, searchField, textBoxBase, getter, setter, mandatory, regex, regexErrorMessage));
    return searchField;
  }

  // --- LIST BOX ---

  public FlowPanel addListBox(String labelText, ListBox listBox, Function<T, String> getter,
    BiConsumer<T, String> setter, boolean mandatory) {
    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    label.addStyleName("form-label");
    if (mandatory) {
      label.addStyleName("form-label-mandatory");
    }

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    listBox.addStyleName("form-listbox");

    inputPanel.add(listBox);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);
    fieldsContainer.add(searchField);

    listBox.addChangeHandler(event -> onChange());

    bindings.add(new ListBoxBinding(labelText, searchField, listBox, getter, setter, mandatory));
    return searchField;
  }

  // --- DATE FIELD ---

  public FlowPanel addDateField(String labelText, DateBox dateBox, Function<T, Date> getter, BiConsumer<T, Date> setter,
    boolean mandatory) {

    FlowPanel searchField = createFieldContainer(labelText, mandatory);
    FlowPanel inputPanel = (FlowPanel) ((FlowPanel) searchField.getWidget(0)).getWidget(1);

    dateBox.addStyleName("form-textbox form-textbox-small");

    inputPanel.add(dateBox);
    fieldsContainer.add(searchField);

    dateBox.addValueChangeHandler(event -> onChange());

    bindings.add(new DateBoxBinding(labelText, searchField, dateBox, getter, setter, mandatory));
    return searchField;
  }

  // --- SEARCH SUGGEST FIELD ---

  public <S extends IsIndexed> FlowPanel addSearchSuggestField(String labelText, SearchSuggestBox<S> suggestBox,
    Function<T, String> getter, BiConsumer<T, String> setter, boolean mandatory) {

    FlowPanel searchField = createFieldContainer(labelText, mandatory);
    FlowPanel inputPanel = (FlowPanel) ((FlowPanel) searchField.getWidget(0)).getWidget(1);

    suggestBox.addStyleName("form-textbox");

    inputPanel.add(suggestBox);
    fieldsContainer.add(searchField);

    suggestBox.addValueChangeHandler(event -> onChange());

    bindings.add(new SearchSuggestBoxBinding<>(labelText, searchField, suggestBox, getter, setter, mandatory));
    return searchField;
  }

  // --- INCREMENTAL LIST FIELD ---

  public FlowPanel addIncrementalListField(String labelText, IncrementalList list, Function<T, List<String>> getter,
    BiConsumer<T, List<String>> setter, boolean mandatory) {

    FlowPanel searchField = createFieldContainer(labelText, mandatory);
    FlowPanel inputPanel = (FlowPanel) ((FlowPanel) searchField.getWidget(0)).getWidget(1);

    inputPanel.add(list);
    fieldsContainer.add(searchField);

    list.addValueChangeHandler(event -> onChange());

    bindings.add(new IncrementalListBinding(labelText, searchField, list, getter, setter, mandatory));
    return searchField;
  }

  private FlowPanel createFieldContainer(String labelText, boolean mandatory) {
    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    if (mandatory) {
      label.setText(labelText + " *");
    }
    label.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);

    return searchField;
  }

  protected void onChange() {
    changed = true;
    ValueChangeEvent.fire(this, getValue());
  }

  private void refreshUI() {
    if (model == null)
      return;
    for (FormBinding<T> binding : bindings) {
      binding.refreshFromModel(model);
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @SuppressWarnings("rawtypes")
  interface GenericDataFormUiBinder extends UiBinder<Widget, GenericDataForm> {
  }

  // --- BINDING INTERFACES & CLASSES ---

  private interface FormBinding<T> {
    String getLabelText();

    void refreshFromModel(T model);

    void flushToModel(T model);

    boolean validate();

    String getRegexErrorMessage();
  }

  private class TextBoxBaseBinding implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final TextBoxBase widget;
    private final Function<T, String> getter;
    private final BiConsumer<T, String> setter;
    private final boolean mandatory;
    private final String regex;
    private final String regexErrorMessage;

    public TextBoxBaseBinding(String labelText, FlowPanel container, TextBoxBase widget, Function<T, String> getter,
      BiConsumer<T, String> setter, boolean mandatory, String regex, String regexErrorMessage) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
      this.setter = setter;
      this.mandatory = mandatory;
      this.regex = regex;
      this.regexErrorMessage = regexErrorMessage;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return regexErrorMessage;
    }

    @Override
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      widget.setText(value != null ? value : "");
    }

    @Override
    public void flushToModel(T model) {
      if (!container.isVisible() || !widget.isEnabled())
        return; // SKIP IF HIDDEN OR DISABLED
      setter.accept(model, widget.getText());
    }

    @Override
    public boolean validate() {
      if (!container.isVisible() || !widget.isEnabled())
        return true; // SKIP IF HIDDEN OR DISABLED

      String text = widget.getText();
      boolean isBlank = (text == null || text.trim().isEmpty());

      if (mandatory && isBlank) {
        widget.addStyleName("isWrong");
        return false;
      }

      if (!isBlank && regex != null && !text.matches(regex)) {
        widget.addStyleName("isWrong");
        return false;
      }

      widget.removeStyleName("isWrong");
      return true;
    }
  }

  private class ListBoxBinding implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final ListBox widget;
    private final Function<T, String> getter;
    private final BiConsumer<T, String> setter;
    private final boolean mandatory;

    public ListBoxBinding(String labelText, FlowPanel container, ListBox widget, Function<T, String> getter,
      BiConsumer<T, String> setter, boolean mandatory) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
      this.setter = setter;
      this.mandatory = mandatory;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return null;
    }

    @Override
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      if (value != null && !value.isEmpty()) {
        for (int i = 0; i < widget.getItemCount(); i++) {
          if (widget.getValue(i).equals(value)) {
            widget.setSelectedIndex(i);
            return;
          }
        }
      }
      if (widget.getItemCount() > 0)
        widget.setSelectedIndex(0);
    }

    @Override
    public void flushToModel(T model) {
      if (!container.isVisible() || !widget.isEnabled())
        return; // SKIP IF HIDDEN
      setter.accept(model, widget.getSelectedValue());
    }

    @Override
    public boolean validate() {
      if (!container.isVisible() || !widget.isEnabled())
        return true; // SKIP IF HIDDEN

      // FIX: Changed <= 0 to < 0 since index 0 is now a valid selection!
      boolean isBlank = widget.getSelectedIndex() < 0 || widget.getSelectedValue().trim().isEmpty();

      if (mandatory && isBlank) {
        widget.addStyleName("isWrong");
        return false;
      }

      widget.removeStyleName("isWrong");
      return true;
    }
  }

  private class ReadOnlyFieldBinding implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final InlineHTML widget;
    private final Function<T, String> getter;

    public ReadOnlyFieldBinding(String labelText, FlowPanel container, InlineHTML widget, Function<T, String> getter) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return null;
    }

    @Override
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      widget.setText(value != null ? value : "");
    }

    @Override
    public void flushToModel(T model) {
    }

    @Override
    public boolean validate() {
      return true;
    }
  }

  private class DateBoxBinding implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final DateBox widget;
    private final Function<T, Date> getter;
    private final BiConsumer<T, Date> setter;
    private final boolean mandatory;

    DateBoxBinding(String labelText, FlowPanel container, DateBox widget, Function<T, Date> getter,
      BiConsumer<T, Date> setter, boolean mandatory) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
      this.setter = setter;
      this.mandatory = mandatory;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return null;
    }

    @Override
    public void refreshFromModel(T model) {
      widget.setValue(getter.apply(model));
    }

    @Override
    public void flushToModel(T model) {
      if (!container.isVisible() || !widget.isEnabled()) {
        return;
      }
      setter.accept(model, widget.getValue());
    }

    @Override
    public boolean validate() {
      if (!container.isVisible() || !widget.isEnabled()) {
        return true;
      }

      Date value = widget.getValue();
      if (mandatory && value == null) {
        widget.addStyleName("isWrong");
        return false;
      }

      widget.removeStyleName("isWrong");
      return true;
    }
  }

  private class SearchSuggestBoxBinding<S extends IsIndexed> implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final SearchSuggestBox<S> widget;
    private final Function<T, String> getter;
    private final BiConsumer<T, String> setter;
    private final boolean mandatory;

    SearchSuggestBoxBinding(String labelText, FlowPanel container, SearchSuggestBox<S> widget,
      Function<T, String> getter, BiConsumer<T, String> setter, boolean mandatory) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
      this.setter = setter;
      this.mandatory = mandatory;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return null;
    }

    @Override
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      widget.setValue(value != null ? value : "");
    }

    @Override
    public void flushToModel(T model) {
      if (!container.isVisible()) {
        return;
      }
      setter.accept(model, widget.getValue());
    }

    @Override
    public boolean validate() {
      if (!container.isVisible()) {
        return true;
      }

      String value = widget.getValue();
      boolean isBlank = value == null || value.trim().isEmpty();

      if (mandatory && isBlank) {
        widget.addStyleName("isWrong");
        return false;
      }

      widget.removeStyleName("isWrong");
      return true;
    }
  }

  private class IncrementalListBinding implements FormBinding<T> {
    private final String labelText;
    private final FlowPanel container;
    private final IncrementalList widget;
    private final Function<T, List<String>> getter;
    private final BiConsumer<T, List<String>> setter;
    private final boolean mandatory;

    IncrementalListBinding(String labelText, FlowPanel container, IncrementalList widget,
      Function<T, List<String>> getter, BiConsumer<T, List<String>> setter, boolean mandatory) {
      this.labelText = labelText;
      this.container = container;
      this.widget = widget;
      this.getter = getter;
      this.setter = setter;
      this.mandatory = mandatory;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public String getRegexErrorMessage() {
      return null;
    }

    @Override
    public void refreshFromModel(T model) {
      List<String> value = getter.apply(model);
      if (value != null) {
        widget.setTextBoxList(value);
      } else {
        widget.clearTextBoxes();
      }
    }

    @Override
    public void flushToModel(T model) {
      if (!container.isVisible()) {
        return;
      }
      setter.accept(model, widget.getTextBoxesValue());
    }

    @Override
    public boolean validate() {
      if (!container.isVisible()) {
        return true;
      }

      List<String> value = widget.getTextBoxesValue();
      if (mandatory && (value == null || value.isEmpty())) {
        widget.addStyleName("isWrong");
        return false;
      }

      widget.removeStyleName("isWrong");
      return true;
    }
  }
}