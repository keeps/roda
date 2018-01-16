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

import org.roda.wui.client.common.utils.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class IncrementalList extends Composite implements HasValueChangeHandlers<List<String>> {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalList> {
  }

  @UiField
  FlowPanel textBoxPanel;

  @UiField
  Button addDynamicButton;

  private ArrayList<RemovableTextBox> textBoxes;
  boolean changed = false;

  public IncrementalList() {
    this(false, null);
  }

  public IncrementalList(boolean vertical) {
    this(vertical, null);
  }

  public IncrementalList(boolean vertical, List<String> initialValues) {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<>();
    if (vertical) {
      addStyleDependentName("vertical");
    }
    if (initialValues == null || initialValues.isEmpty()) {
      // make sure there is one text box
      addTextBox(null);
    } else {
      setTextBoxList(initialValues);
    }
  }

  public List<String> getTextBoxesValue() {
    ArrayList<String> listValues = new ArrayList<>();
    for (RemovableTextBox textBox : textBoxes) {
      if (StringUtils.isNotBlank(textBox.getTextBoxValue())) {
        listValues.add(textBox.getTextBoxValue());
      }
    }
    return listValues;
  }

  public void setTextBoxList(List<String> list) {
    clearTextBoxes();
    for (int i = list.size() - 1; i >= 0; i--) {
      addTextBox(list.get(i));
    }
  }

  public void clearTextBoxes() {
    textBoxPanel.clear();
    textBoxes = new ArrayList<>();
    addDynamicButton.setVisible(true);
  }

  @UiHandler("addDynamicButton")
  void addMore(ClickEvent event) {
    addTextBox(null);
  }

  private void addTextBox(String element) {
    final RemovableTextBox box = new RemovableTextBox(textBoxes.isEmpty(), element);
    textBoxPanel.insert(box, 0);
    textBoxes.add(0, box);
    addDynamicButton.setVisible(false);

    box.addRemoveClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (box.isFirst()) {
          addTextBox(null);
        } else {
          textBoxPanel.remove(box);
          textBoxes.remove(box);
          if (textBoxes.isEmpty()) {
            addDynamicButton.setVisible(true);
          }
        }

        ValueChangeEvent.fire(IncrementalList.this, getTextBoxesValue());
      }
    });

    box.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        ValueChangeEvent.fire(IncrementalList.this, getTextBoxesValue());
      }
    });

    shiftValuesDown();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  private void shiftValuesDown() {
    for (int i = 0; i < textBoxes.size() - 1; i++) {
      textBoxes.get(i).item.setText(textBoxes.get(i + 1).getTextBoxValue());
    }
    textBoxes.get(textBoxes.size() - 1).item.setText(null);
  }
}
