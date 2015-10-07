/**
 * 
 */
package org.roda.wui.dissemination.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.DescriptionObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationMessages;

/**
 * @author Luis Faria
 * 
 */
public class DescriptionGroupPanel extends FlowPanel implements SourcesChangeEvents {

  private static DisseminationMessages messages = (DisseminationMessages) GWT.create(DisseminationMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DescriptionObject object;

  private final String name;

  private final List<DescriptionElement> elements;

  private final Label header;

  private final FlowPanel content;

  // private int rows;

  private boolean readonly;

  private boolean optionalVisible;

  private List<ChangeListener> listeners;

  public DescriptionGroupPanel(String name, DescriptionObject object) {
    this(name, object, true);
  }

  public DescriptionGroupPanel(String name, DescriptionObject object, boolean optionalVisible) {
    this.name = name;
    this.object = object;
    readonly = true;
    this.optionalVisible = optionalVisible;
    listeners = new Vector<ChangeListener>();

    elements = new Vector<DescriptionElement>();
    header = new Label(name);
    content = new FlowPanel();
    add(header);
    add(content);

    this.addStyleName("descriptionGroup");
    header.addStyleName("descriptionGroup-header");
    content.addStyleName("descriptionGroup-content");
    this.setVisible(false);
  }

  public void addElement(DescriptionElement element) {
    elements.add(element);
    element.setReadonly(readonly);
    insertElementInTable(element);
    element.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        DescriptionGroupPanel.this.onChange(sender);
      }

    });
  }

  protected void insertElementInTable(DescriptionElement element) {

    Label label = new Label(messages.descriptionLabel(element.getDescription()));
    SimplePanel body = element.getBody();

    content.add(label);
    content.add(body);

    boolean visible = element.isSet() || (!readonly && (element.isRequired() || optionalVisible));

    label.setVisible(visible);
    body.setVisible(visible);

    // table.getCellFormatter().addStyleName(rows, 0, "label-container");
    // table.getCellFormatter().addStyleName(rows, 1, "value-container");
    //
    // table.getCellFormatter().addStyleName(rows, 0,
    // "label-container-last");
    // table.getCellFormatter().addStyleName(rows, 1,
    // "value-container-last");
    //
    // table.getRowFormatter().addStyleName(rows, "descriptionGroup-row");

    // table.getRowFormatter().setVisible(rows, element.isSet() ||
    // !readonly);
    // if (element.isSet() || (!readonly && (element.isRequired() ||
    // optionalVisible))) {
    // table.getRowFormatter().setVisible(rows, true);
    // this.setVisible(true);
    //
    // } else {
    // table.getRowFormatter().setVisible(rows, false);
    // }
    //
    // if (rows > 0) {
    // table.getCellFormatter().removeStyleName(rows - 1, 0,
    // "label-container-last");
    // table.getCellFormatter().removeStyleName(rows - 1, 1,
    // "value-container-last");
    // }

    label.addStyleName("descriptionGroup-label");
    label.addStyleName("descriptiveMetadata-field-key");
    body.addStyleName("descriptionGroup-value");

    // rows++;
  }

  public boolean isReadonly() {
    return readonly;
  }

  public void setReadonly(boolean readonly) {
    if (this.readonly != readonly) {
      this.readonly = readonly;
      updateVisibility();
    }
  }

  public boolean isOptionalVisible() {
    return optionalVisible;
  }

  public void setOptionalVisible(boolean optionalVisible) {
    if (this.optionalVisible != optionalVisible) {
      this.optionalVisible = optionalVisible;
      updateVisibility();
    }
  }

  public void updateVisibility() {
    int visibleItemsCount = 0;
    for (int i = 0; i < elements.size(); i++) {
      DescriptionElement element = (DescriptionElement) elements.get(i);
      element.setReadonly(readonly);
      Widget labelWidget = content.getWidget(2 * i);
      Widget bodyWidget = content.getWidget(2 * i + 1);
      if (element.isSet() || (!readonly && (element.isRequired() || optionalVisible))) {
        labelWidget.setVisible(true);
        bodyWidget.setVisible(true);
        visibleItemsCount++;
      } else {
        labelWidget.setVisible(false);
        bodyWidget.setVisible(false);
      }

    }
    this.setVisible(visibleItemsCount > 0);
  }

  public void save() {
    for (DescriptionElement element : elements) {
      element.save();
    }
  }

  public void cancel() {
    for (DescriptionElement element : elements) {
      element.cancel();
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (DescriptionElement element : elements) {
      valid &= element.isValid();
    }

    return valid;
  }

  public boolean isChanged() {
    boolean changed = false;
    for (DescriptionElement element : elements) {
      changed |= element.isChanged();
    }

    return changed;
  }

}
