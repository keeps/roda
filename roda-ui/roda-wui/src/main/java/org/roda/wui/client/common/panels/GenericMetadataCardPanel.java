package org.roda.wui.client.common.panels;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.common.ActionsToolbar;

/**
 * A generic composite card panel specifically for descriptive metadata.
 * * @param <T> The data model type.
 * 
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class GenericMetadataCardPanel<T> extends Composite {

  protected final FlowPanel metadataContainer;
  private final FlowPanel mainContainer;
  private final FlowPanel headerContainer;
  private final FlowPanel bodyContainer;

  protected GenericMetadataCardPanel() {
    mainContainer = new FlowPanel();
    mainContainer.setStyleName("roda6Card");

    // Initialize the header container, but don't add it to the DOM yet
    headerContainer = new FlowPanel();
    headerContainer.setStyleName("cardHeader");

    bodyContainer = new FlowPanel();
    bodyContainer.setStyleName("cardBody");

    metadataContainer = new FlowPanel();
    metadataContainer.setStyleName("generic-metadata-panel");

    bodyContainer.add(metadataContainer);
    mainContainer.add(bodyContainer);

    initWidget(mainContainer);
  }

  protected void addSeparator(String labelText) {
    Label separatorLabel = new Label(labelText);
    separatorLabel.addStyleName("form-separator");
    metadataContainer.add(separatorLabel);
  }

  /**
   * Starts the fluent builder for adding a new field to the metadata panel.
   * 
   * @param labelText
   *          The text for the field label.
   * @return A new FieldBuilder instance.
   */
  protected FieldBuilder buildField(String labelText) {
    return new FieldBuilder(labelText);
  }

  /**
   * Core template method. It handles clearing, building the header, and building
   * the fields dynamically based on the provided data.
   */
  public final void setData(T data) {
    if (data == null) {
      metadataContainer.clear();
      return;
    }

    // 1. Process Header
    FlowPanel headerPanel = createHeaderWidget(data);

    // Check if the panel exists AND actually contains widgets
    if (headerPanel != null && headerPanel.getWidgetCount() != 0) {
      mainContainer.setStyleName("roda6CardWithHeader");
      headerContainer.clear();

      ActionsToolbar actionsToolbar = new ActionsToolbar();
      actionsToolbar.setActionableMenu(headerPanel);
      actionsToolbar.setLabelVisible(false);
      actionsToolbar.setTagsVisible(false);
      headerContainer.add(actionsToolbar);

      // Insert header at the top if it's not already attached
      if (mainContainer.getWidgetIndex(headerContainer) == -1) {
        mainContainer.insert(headerContainer, 0);
      }
    } else {
      // Fallback to standard style if the FlowPanel was empty or null
      mainContainer.setStyleName("roda6Card");
      if (mainContainer.getWidgetIndex(headerContainer) != -1) {
        mainContainer.remove(headerContainer);
      }
    }

    // 2. Process Fields
    metadataContainer.clear();
    buildFields(data);
  }

  /**
   * Developers must define the header widget (e.g., ActionableWidgetBuilder).
   * 
   * @return The Widget to go inside the header, or null if no header is needed.
   */
  protected abstract FlowPanel createHeaderWidget(T data);

  /**
   * Developers must define how data maps to fields.
   */
  protected abstract void buildFields(T data);

  // ==========================================
  // INNER BUILDER CLASS
  // ==========================================

  public class FieldBuilder {
    private final String labelText;
    private String textValue;
    private SafeHtml htmlValue;
    private Widget widgetValue;
    private ClickHandler clickHandler;
    private String valueStyleName;
    private boolean isPreCode = false;

    private FieldBuilder(String labelText) {
      this.labelText = labelText;
    }

    public FieldBuilder withValue(String value) {
      this.textValue = value;
      return this;
    }

    public FieldBuilder withHtml(SafeHtml html) {
      this.htmlValue = html;
      return this;
    }

    public FieldBuilder withWidget(Widget widget) {
      this.widgetValue = widget;
      return this;
    }

    public FieldBuilder asPreCode() {
      this.isPreCode = true;
      return this;
    }

    public FieldBuilder onClick(ClickHandler handler) {
      this.clickHandler = handler;
      return this;
    }

    public FieldBuilder withValueStyleName(String styleName) {
      this.valueStyleName = styleName;
      return this;
    }

    /**
     * Finalizes the field. If a valid value is present, it constructs the DOM
     * elements and appends them to the metadata container.
     */
    public void build() {
      Widget finalContentWidget = null;

      // 1. Determine what type of content we are rendering and validate it's not
      // empty
      if (widgetValue != null) {
        finalContentWidget = widgetValue;
      } else if (htmlValue != null && htmlValue.asString() != null && !htmlValue.asString().trim().isEmpty()) {
        finalContentWidget = new InlineHTML(htmlValue);
      } else if (textValue != null && !textValue.trim().isEmpty()) {
        finalContentWidget = new InlineHTML(textValue);
      }

      // 2. If all values were null or empty, abort building (mimics 'IfNotNull')
      if (finalContentWidget == null) {
        return;
      }

      if (valueStyleName != null && !valueStyleName.trim().isEmpty()) {
        finalContentWidget.addStyleName(valueStyleName);
      }

      // 3. Apply ClickHandler if requested (only works natively with InlineHTML in
      // this setup)
      if (clickHandler != null && finalContentWidget instanceof InlineHTML) {
        ((InlineHTML) finalContentWidget).addClickHandler(clickHandler);
        finalContentWidget.addStyleName("btn-link addCursorPointer");
      }

      // 4. Construct the DOM
      FlowPanel fieldDiv = new FlowPanel();
      fieldDiv.setStyleName("field");

      FlowPanel labelDiv = new FlowPanel();
      labelDiv.setStyleName("label");
      labelDiv.getElement().setInnerText(labelText);

      FlowPanel valueDiv = new FlowPanel();
      valueDiv.setStyleName("value");
      valueDiv.add(finalContentWidget);

      if (isPreCode) {
        valueDiv.addStyleName("code-pre");
        valueDiv.addStyleName("notification-body-content");
      }

      // 5. Assemble and append
      fieldDiv.add(labelDiv);
      fieldDiv.add(valueDiv);
      metadataContainer.add(fieldDiv);
    }
  }
}