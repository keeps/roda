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
import java.util.Map;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.NamedIndexedModel;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.wui.client.common.dialogs.SelectDialog;
import org.roda.wui.client.common.dialogs.SelectDialogFactory;
import org.roda.wui.client.common.utils.StringUtils;

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

import config.i18n.client.ClientMessages;

public class IncrementalAssociativeList extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("rawtypes")
  interface MyUiBinder extends UiBinder<Widget, IncrementalAssociativeList> {
  }

  @UiField
  FlowPanel textBoxPanel;

  @UiField
  Button addDynamicTextBoxButton;

  private ArrayList<RemovableAssociativeTextBox> textBoxes;
  private String dialogName;
  private String idAttribute;
  private String searchAttribute;
  private Class actualClass;
  private List<String> excludedIds;
  boolean changed = false;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public <T extends IsIndexed> IncrementalAssociativeList(Class<T> specificClass, String id, String search,
    String dialogName) {
    textBoxes = new ArrayList<RemovableAssociativeTextBox>();
    excludedIds = new ArrayList<String>();

    setIdAttribute(id);
    setSearchAttribute(search);
    setDialogName(dialogName);
    setClass(specificClass);

    initWidget(uiBinder.createAndBindUi(this));
  }

  public List<String> getTextBoxesValue() {
    ArrayList<String> listValues = new ArrayList<String>();
    for (RemovableAssociativeTextBox textBox : textBoxes) {
      if (StringUtils.isNotBlank(textBox.getHiddenTextBoxValue())) {
        listValues.add(textBox.getHiddenTextBoxValue());
      }
    }
    return listValues;
  }

  public void setTextBoxList(Map<String, String> map) {
    for (String element : map.keySet()) {
      addTextBox(element, map.get(element));
    }
  }

  public void setDialogName(String name) {
    dialogName = name;
  }

  public void setIdAttribute(String id) {
    idAttribute = id;
  }

  public void setSearchAttribute(String search) {
    searchAttribute = search;
  }

  public <T extends IsIndexed> void setClass(Class<T> specificClass) {
    actualClass = specificClass;
  }

  public void setExcludedIds(List<String> ids) {
    excludedIds = ids;
  }

  public void clearTextBoxes() {
    textBoxPanel.clear();
    textBoxes = new ArrayList<RemovableAssociativeTextBox>();
  }

  @UiHandler("addDynamicTextBoxButton")
  void addMoreTextBox(ClickEvent event) {
    addTextBox(null, null);
  }

  private void addTextBox(String elementId, String elementName) {
    final RemovableAssociativeTextBox box = new RemovableAssociativeTextBox(elementId, elementName);
    textBoxPanel.add(box);
    textBoxes.add(box);
    box.setVisible(false);

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
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalAssociativeList.this);
      }
    });

    box.addSearchClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {

        BasicSearchFilterParameter searchParam = new BasicSearchFilterParameter(searchAttribute, "*");
        FilterParameter[] idsToExclude = new FilterParameter[textBoxes.size() + excludedIds.size() + 1];
        int counterIdFilter = 0;

        for (RemovableAssociativeTextBox otherBox : textBoxes) {
          idsToExclude[counterIdFilter] = new NotSimpleFilterParameter(idAttribute, otherBox.getHiddenTextBoxValue());
          counterIdFilter++;
        }

        for (String id : excludedIds) {
          idsToExclude[counterIdFilter] = new NotSimpleFilterParameter(idAttribute, id);
          counterIdFilter++;
        }

        idsToExclude[counterIdFilter] = searchParam;
        Filter filter = new Filter(idsToExclude);

        SelectDialogFactory dialogFactory = new SelectDialogFactory();

        try {
          SelectDialog dialog = dialogFactory.getSelectDialog(actualClass, dialogName, filter, false);
          dialog.showAndCenter();

          ValueChangeHandler<NamedIndexedModel> changeHandler = new ValueChangeHandler<NamedIndexedModel>() {

            @Override
            public void onValueChange(ValueChangeEvent<NamedIndexedModel> event) {
              NamedIndexedModel modelValue = event.getValue();
              box.setVisible(true);
              box.setNameTextBoxValue(modelValue.getName());
              box.setHiddenTextBoxValue(modelValue.getUUID());
              DomEvent.fireNativeEvent(Document.get().createChangeEvent(), box);
            }
          };

          dialog.addValueChangeHandler(changeHandler);

        } catch (NotFoundException e) {
          GWT.log(actualClass.getSimpleName() + " dialog not found: " + e);
        }

      }
    });
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

}
