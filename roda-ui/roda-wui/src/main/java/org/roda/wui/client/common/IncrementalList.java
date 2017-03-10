/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class IncrementalList extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalList> {
  }

  @UiField
  FlowPanel textBoxPanel;

  @UiField
  Button addDynamicTextBoxButton;

  private ArrayList<RemovableTextBox> textBoxes;
  boolean changed = false;

  public IncrementalList() {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<RemovableTextBox>();
  }

  public IncrementalList(List<String> list, String label) {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<RemovableTextBox>();
    this.setTextBoxList(list);
  }

  public List<String> getTextBoxesValue() {
    ArrayList<String> listValues = new ArrayList<>();
    for (RemovableTextBox textBox : textBoxes) {
      listValues.add(textBox.getTextBoxValue());
    }
    return listValues;
  }

  public void setTextBoxList(List<String> list) {
    for (String element : list) {
      addTextBox(element);
    }
  }

  public void clearTextBoxes() {
    textBoxPanel.clear();
    textBoxes = new ArrayList<RemovableTextBox>();
  }

  @UiHandler("addDynamicTextBoxButton")
  void addMoreTextBox(ClickEvent event) {
    addTextBox(null);
  }

  private void addTextBox(String element) {
    final RemovableTextBox box = new RemovableTextBox(element);
    textBoxPanel.add(box);
    textBoxes.add(box);

    box.addRemoveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        textBoxPanel.remove(box);
        textBoxes.remove(box);
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), box);
      }
    });

    box.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalList.this);
      }
    });
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

}
