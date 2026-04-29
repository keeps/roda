package org.roda.wui.client.common.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class GenericDataForm<T> extends Composite implements GenericDataPanel<T>, HasValueChangeHandlers<T> {

  private static GenericDataFormUiBinder uiBinder = GWT.create(GenericDataFormUiBinder.class);

  // Use the new Interface so we can mix TextBoxes and InlineHTML bindings
  private final List<FormBinding> bindings = new ArrayList<>();

  @UiField
  FlowPanel fieldsContainer;

  @UiField
  HTML errors;

  private T model;
  private boolean changed = false;

  public GenericDataForm() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Initializes the form with the object to be edited.
   */
  public void setModel(T model) {
    this.model = model;
    this.changed = false;
    refreshUI();
  }

  @Override
  public T getValue() {
    // Flush UI values back to the model
    for (FormBinding binding : bindings) {
      binding.flushToModel(model);
    }
    return model;
  }

  @Override
  public boolean isValid() {
    List<String> errorMessages = new ArrayList<>();
    boolean valid = true;

    for (FormBinding binding : bindings) {
      if (!binding.validate()) {
        valid = false;
        errorMessages.add("Field '" + binding.getLabelText() + "' is invalid or mandatory.");
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

  /**
   * Adds a generic read-only field to the form dynamically using InlineHTML.
   *
   * @param labelText The label to display
   * @param getter    Function to extract the value from the model
   */
  public void addReadOnlyField(String labelText, Function<T, String> getter) {
    addReadOnlyField(labelText, getter, false);
  }

  /**
   * Adds a generic read-only field to the form dynamically using InlineHTML.
   *
   * @param labelText The label to display
   * @param getter    Function to extract the value from the model
   * @param mandatory Whether this field is visually marked as required
   */
  public void addReadOnlyField(String labelText, Function<T, String> getter, boolean mandatory) {
    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    if (mandatory) {
      label.setText(labelText + "*");
    }
    label.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    InlineHTML inlineHTML = new InlineHTML();
    // Use setText instead of setHTML to automatically escape inputs and prevent XSS
    inlineHTML.addStyleName("form-readonly-value");

    // Assemble the DOM
    inputPanel.add(inlineHTML);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);

    // Add to main container
    fieldsContainer.add(searchField);

    // Register the read-only binding
    bindings.add(new ReadOnlyFieldBinding(labelText, inlineHTML, getter));
  }

  public void addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
                           boolean mandatory) {
    addTextField(labelText, getter, setter, mandatory, false);
  }

  public void addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
                           boolean mandatory, boolean readOnly) {

    // Route to the new InlineHTML method if readOnly is true
    if (readOnly) {
      addReadOnlyField(labelText, getter, mandatory);
      return;
    }

    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    if (mandatory) {
      label.setText(labelText + "*");
    }

    label.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    TextBox textBox = new TextBox();
    textBox.addStyleName("form-textbox");

    // Assemble the DOM
    inputPanel.add(textBox);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);

    // Add to main container
    fieldsContainer.add(searchField);

    // Setup handlers to track changes
    ChangeHandler changeHandler = event -> onChange();
    KeyUpHandler keyUpHandler = event -> onChange();
    textBox.addChangeHandler(changeHandler);
    textBox.addKeyUpHandler(keyUpHandler);

    // Register the binding
    bindings.add(new TextFieldBinding(labelText, textBox, getter, setter, mandatory, null, null));
  }

  public void addTextField(String labelText, Function<T, String> getter, BiConsumer<T, String> setter,
                           boolean mandatory, boolean readOnly, String regex, String regexErrorMessage) {

    // Route to the new InlineHTML method if readOnly is true
    if (readOnly) {
      addReadOnlyField(labelText, getter, mandatory);
      return;
    }

    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    if (mandatory) {
      label.setText(labelText + "*");
    }
    label.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    TextBox textBox = new TextBox();
    textBox.addStyleName("form-textbox");

    inputPanel.add(textBox);
    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);
    fieldsContainer.add(searchField);

    ChangeHandler changeHandler = event -> onChange();
    KeyUpHandler keyUpHandler = event -> onChange();
    textBox.addChangeHandler(changeHandler);
    textBox.addKeyUpHandler(keyUpHandler);

    // Register binding with regex parameters
    bindings.add(new TextFieldBinding(labelText, textBox, getter, setter, mandatory, regex, regexErrorMessage));
  }

  protected void onChange() {
    changed = true;
    ValueChangeEvent.fire(this, getValue());
  }

  private void refreshUI() {
    if (model == null)
      return;
    for (FormBinding binding : bindings) {
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

  /**
   * Base interface for all field bindings to allow mixing different UI Widgets.
   */
  private interface FormBinding<T> {
    String getLabelText();
    void refreshFromModel(T model);
    void flushToModel(T model);
    boolean validate();
  }

  /**
   * Internal class to hold the relationship between the TextBox Widget and the Model data
   */
  private class TextFieldBinding implements FormBinding<T> {
    private final String labelText;
    private final TextBox widget;
    private final Function<T, String> getter;
    private final BiConsumer<T, String> setter;
    private final boolean mandatory;
    private final String regex;
    private final String regexErrorMessage;

    public TextFieldBinding(String labelText, TextBox widget, Function<T, String> getter, BiConsumer<T, String> setter,
                            boolean mandatory, String regex, String regexErrorMessage) {
      this.labelText = labelText;
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
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      widget.setText(value != null ? value : "");
    }

    @Override
    public void flushToModel(T model) {
      setter.accept(model, widget.getText());
    }

    @Override
    public boolean validate() {
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

    public String getRegexErrorMessage() {
      return regexErrorMessage;
    }
  }

  /**
   * Internal class to hold the relationship between the ReadOnly InlineHTML Widget and the Model data
   */
  private class ReadOnlyFieldBinding implements FormBinding<T> {
    private final String labelText;
    private final InlineHTML widget;
    private final Function<T, String> getter;

    public ReadOnlyFieldBinding(String labelText, InlineHTML widget, Function<T, String> getter) {
      this.labelText = labelText;
      this.widget = widget;
      this.getter = getter;
    }

    @Override
    public String getLabelText() {
      return labelText;
    }

    @Override
    public void refreshFromModel(T model) {
      String value = getter.apply(model);
      widget.setText(value != null ? value : "");
    }

    @Override
    public void flushToModel(T model) {
      // Read-only fields do not modify the underlying model
    }

    @Override
    public boolean validate() {
      // Read-only fields cannot be invalidly edited by the user
      return true;
    }
  }
}