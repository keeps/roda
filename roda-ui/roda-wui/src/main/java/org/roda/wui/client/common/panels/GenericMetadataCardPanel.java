package org.roda.wui.client.common.panels;

import java.util.function.Function;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

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

  // The container that holds the key-value pairs
  protected FlowPanel metadataContainer;
  private final FlowPanel mainContainer;
    private final FlowPanel bodyContainer;

  public GenericMetadataCardPanel() {
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

  public GenericMetadataCardPanel(Widget headerWidget) {
    // <div class="roda6CardWithHeader">
    mainContainer = new FlowPanel();
    mainContainer.setStyleName("roda6CardWithHeader");

    // <div class="cardHeader">
    if (headerWidget != null || headerWidger.getWidgetCount() == 0) {
        FlowPanel headerContainer = new FlowPanel();
      headerContainer.setStyleName("cardHeader");
      headerContainer.add(headerWidget);
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

  /**
   * The generic method to add a field only if the getter returns a
   * non-null/non-empty value. * @param labelText The text to display in the label
   * div.
   * 
   * @param getter
   *          The Function method reference to extract the value from the data
   *          object.
   * @param data
   *          The actual data object.
   */
  protected void addFieldIfNotNull(String labelText, Function<T, String> getter, T data) {
    if (data == null)
      return;

    // Execute the getter function
    String value = getter.apply(data);

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
      valueDiv.add(new InlineHTML(value));

      // Assemble
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);

      // Append to the metadata container
      metadataContainer.add(fieldDiv);
    }
  }

  protected void addFieldIfNotNull(String labelText, String value) {
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
      valueDiv.add(new InlineHTML(value));

      // Assemble
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);

      // Append to the metadata container
      metadataContainer.add(fieldDiv);
    }
  }

  protected void addFieldIfNotNull(String labelText, SafeHtml value) {
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
      valueDiv.add(new InlineHTML(value));

      // Assemble
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);

      // Append to the metadata container
      metadataContainer.add(fieldDiv);
    }
  }

  /**
   * Concrete classes implement this to pass their specific model and map the
   * fields.
   */
  public abstract void setData(T data);
}