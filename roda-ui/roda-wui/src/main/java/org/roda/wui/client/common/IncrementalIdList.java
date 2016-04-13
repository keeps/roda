package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.NotSimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class IncrementalIdList extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalIdList> {
  }

  @UiField
  FlowPanel textBoxPanel;

  @UiField
  Button addDynamicTextBoxButton;

  private ArrayList<RemovableTextBoxForIDs> textBoxes;
  private static String MODEL_NAME = "Formats";
  boolean changed = false;

  public IncrementalIdList() {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<RemovableTextBoxForIDs>();
  }

  public IncrementalIdList(Map<String, String> map, String label) {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<RemovableTextBoxForIDs>();
    this.setTextBoxList(map);
  }

  public List<String> getTextBoxesValue() {
    ArrayList<String> listValues = new ArrayList<String>();
    for (RemovableTextBoxForIDs textBox : textBoxes) {
      listValues.add(textBox.getHiddenTextBoxValue());
    }
    return listValues;
  }

  public void setTextBoxList(Map<String, String> map) {
    for (String element : map.keySet()) {
      addTextBox(element, map.get(element));
    }
  }

  public void clearTextBoxes() {
    textBoxPanel.clear();
    textBoxes = new ArrayList<RemovableTextBoxForIDs>();
  }

  @UiHandler("addDynamicTextBoxButton")
  void addMoreTextBox(ClickEvent event) {
    addTextBox(null, null);
  }

  private void addTextBox(String elementId, String elementName) {
    final RemovableTextBoxForIDs box = new RemovableTextBoxForIDs(elementId, elementName);
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
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalIdList.this);
      }
    });

    box.addSearchClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {

        BasicSearchFilterParameter searchParam = new BasicSearchFilterParameter(RodaConstants.FORMAT_SEARCH, "*");
        FilterParameter[] idsToExclude = new FilterParameter[textBoxes.size() + 1];
        int counterToAddId = 0;

        for (RemovableTextBoxForIDs otherBox : textBoxes) {
          idsToExclude[counterToAddId] = new NotSimpleFilterParameter(RodaConstants.FORMAT_ID,
            otherBox.getHiddenTextBoxValue());
          counterToAddId++;
        }

        idsToExclude[counterToAddId] = searchParam;
        Filter filter = new Filter(idsToExclude);

        SelectFormatDialog dialog = new SelectFormatDialog(MODEL_NAME, filter);
        dialog.showAndCenter();

        ValueChangeHandler<Format> handler = new ValueChangeHandler<Format>() {

          @Override
          public void onValueChange(ValueChangeEvent<Format> event) {
            box.setNameTextBoxValue(event.getValue().getName());
            box.setHiddenTextBoxValue(event.getValue().getId());
          }
        };

        dialog.addValueChangeHandler(handler);

        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), box);
      }
    });
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

}
