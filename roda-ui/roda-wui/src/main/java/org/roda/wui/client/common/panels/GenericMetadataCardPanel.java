package org.roda.wui.client.common.panels;

import java.util.function.Function;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.common.ActionsToolbar;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

/**
 * A generic composite card panel specifically for descriptive metadata.
 * 
 * @param <T>
 *          The data model type.
 */
public abstract class GenericMetadataCardPanel<T> extends Composite {

  private final FlowPanel mainContainer;
  private final FlowPanel bodyContainer;
  // The container that holds the key-value pairs
  protected FlowPanel metadataContainer;

  protected GenericMetadataCardPanel() {
    mainContainer = new FlowPanel();
    mainContainer.setStyleName("roda6Card");

    // <div class="cardBody">
    bodyContainer = new FlowPanel();
    bodyContainer.setStyleName("cardBody");

    // <div class="descriptiveMetadata">
    metadataContainer = new FlowPanel();
    metadataContainer.setStyleName("generic-metadata-panel");

    bodyContainer.add(metadataContainer);
    mainContainer.add(bodyContainer);

    initWidget(mainContainer);
  }

  protected GenericMetadataCardPanel(FlowPanel header) {
    // <div class="roda6CardWithHeader">
    mainContainer = new FlowPanel();
    mainContainer.setStyleName("roda6CardWithHeader");

    // <div class="cardHeader">
    if (header != null && header.getWidgetCount() != 0) {
      FlowPanel headerContainer = new FlowPanel();
      headerContainer.setStyleName("cardHeader");
      ActionsToolbar actionsToolbar = new ActionsToolbar();
      actionsToolbar.setActionableMenu(header);
      actionsToolbar.setLabelVisible(false);
      actionsToolbar.setTagsVisible(false);
      headerContainer.add(actionsToolbar);
      mainContainer.add(headerContainer);
    }

    // <div class="cardBody">
    bodyContainer = new FlowPanel();
    bodyContainer.setStyleName("cardBody");

    // <div class="descriptiveMetadata">
    metadataContainer = new FlowPanel();
    metadataContainer.setStyleName("generic-metadata-panel");

    bodyContainer.add(metadataContainer);
    mainContainer.add(bodyContainer);

    initWidget(mainContainer);
  }

  protected void addSeparator(String labelText) {
    Label separatorLabel = new Label(labelText);
    separatorLabel.addStyleName("form-separator");

    // Append to the metadata container
    metadataContainer.add(separatorLabel);
  }

  protected void addFieldIfNotNull(String labelText, Function<T, String> getter, T data) {
    if (data == null)
      return;

    // Execute the getter function
    String value = getter.apply(data);

    addFieldIfNotNull(labelText, value);
  }

  protected void addFieldIfNotNull(String labelText, Function<T, String> getter, T data, ClickHandler onClick) {
    if (data == null)
      return;

    // Execute the getter function
    String value = getter.apply(data);

    addFieldIfNotNull(labelText, value, onClick);
  }

  protected void addFieldIfNotNull(String labelText, String value) {
    addFieldIfNotNull(labelText, value, null);
  }

  protected void addFieldIfNotNull(String labelText, String value, ClickHandler onClick) {
    // Only build the DOM elements if the value exists
    if (value != null && !value.trim().isEmpty()) {

      // <div class="field">
      FlowPanel fieldDiv = new FlowPanel();
      fieldDiv.setStyleName("field");

      // <div class="label">Identifier</div>
      FlowPanel labelDiv = new FlowPanel();
      labelDiv.setStyleName("label");
      labelDiv.getElement().setInnerText(labelText);

      // <div class="value"><span class="gwt-InlineHTML">...</span></div>
      FlowPanel valueDiv = new FlowPanel();
      valueDiv.setStyleName("value");
      InlineHTML inlineHTML = new InlineHTML(value);
      if (onClick != null) {
        inlineHTML.addClickHandler(onClick);
        inlineHTML.addStyleName("btn-link addCursorPointer");
      }
      valueDiv.add(inlineHTML);

      // Assemble
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);

      // Append to the metadata container
      metadataContainer.add(fieldDiv);
    }
  }

  protected void addFieldIfNotNull(String labelText, SafeHtml value) {
    addFieldIfNotNull(labelText, value, null);
  }

  protected void addFieldIfNotNull(String labelText, SafeHtml value, ClickHandler onClick) {
    if (value == null)
      return;

    // Only build the DOM elements if the value exists
    if (value.asString() != null && !value.asString().trim().isEmpty()) {

      // <div class="field">
      FlowPanel fieldDiv = new FlowPanel();
      fieldDiv.setStyleName("field");

      // <div class="label">Identifier</div>
      FlowPanel labelDiv = new FlowPanel();
      labelDiv.setStyleName("label");
      labelDiv.getElement().setInnerText(labelText);

      // <div class="value"><span class="gwt-InlineHTML">...</span></div>
      FlowPanel valueDiv = new FlowPanel();
      valueDiv.setStyleName("value");
      InlineHTML inlineHTML = new InlineHTML(value);
      if (onClick != null) {
        inlineHTML.addClickHandler(onClick);
        inlineHTML.addStyleName("btn-link addCursorPointer");
      }
      valueDiv.add(inlineHTML);

      // Assemble
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);

      // Append to the metadata container
      metadataContainer.add(fieldDiv);
    }
  }

  protected void addPreCodeFieldIfNotNull(String labelText, Widget widget) {

    if (widget == null) {
      return;
    }

    // <div class="field">
    FlowPanel fieldDiv = new FlowPanel();
    fieldDiv.setStyleName("field");

    // <div class="label">Identifier</div>
    FlowPanel labelDiv = new FlowPanel();
    labelDiv.setStyleName("label");
    labelDiv.getElement().setInnerText(labelText);

    FlowPanel valueDiv = new FlowPanel();
    valueDiv.setStyleName("value");
    valueDiv.addStyleName("code-pre");
    valueDiv.addStyleName("notification-body-content");
    valueDiv.add(widget);

    // Assemble
    fieldDiv.add(labelDiv);
    fieldDiv.add(valueDiv);

    // Append to the metadata container
    metadataContainer.add(fieldDiv);
  }

  /**
   * Concrete classes implement this to pass their specific model and map the
   * fields.
   */
  public abstract void setData(T data);
}