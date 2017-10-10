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

public class IncrementalFilterList extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalFilterList> {
  }

  @UiField
  FlowPanel filterPanel;

  @UiField
  Button addDynamicButton;

  private ArrayList<RemovableRepresentationInformationFilter> filters;
  boolean changed = false;
  private Map<String, List<String>> fields;

  public IncrementalFilterList() {
    initWidget(uiBinder.createAndBindUi(this));
    filters = new ArrayList<>();
  }

  public List<String> getFiltersValue() {
    ArrayList<String> listValues = new ArrayList<>();
    for (RemovableRepresentationInformationFilter filter : filters) {
      listValues.add(filter.getValue());
    }
    return listValues;
  }

  public void setFields(Map<String, List<String>> fields) {
    this.fields = fields;
  }

  public void setFilters(List<String> filterList) {
    for (String filter : filterList) {
      addFilter(filter);
    }
  }

  public void clearFilters() {
    filterPanel.clear();
    filters = new ArrayList<>();
  }

  @UiHandler("addDynamicButton")
  void addMore(ClickEvent event) {
    addFilter("");
  }

  private void addFilter(String filterString) {
    final RemovableRepresentationInformationFilter filter = new RemovableRepresentationInformationFilter(filterString,
      fields);
    filterPanel.add(filter);
    filters.add(filter);

    filter.addRemoveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        filterPanel.remove(filter);
        filters.remove(filter);
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), filter);
      }
    });

    filter.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalFilterList.this);
      }
    });
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

}
