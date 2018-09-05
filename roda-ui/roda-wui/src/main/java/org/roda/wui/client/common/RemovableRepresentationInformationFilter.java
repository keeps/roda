/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;
import java.util.Map;

import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RemovableRepresentationInformationFilter extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, RemovableRepresentationInformationFilter> {
  }

  @UiField
  ListBox items;

  @UiField
  ListBox fields;

  @UiField
  TextBox value;

  @UiField(provided = true)
  Anchor removeDynamicButton;

  public RemovableRepresentationInformationFilter(String filter, final Map<String, List<String>> fields) {
    removeDynamicButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
    initWidget(uiBinder.createAndBindUi(this));
    fillSelectBoxes(fields);

    if (StringUtils.isNotBlank(filter)) {
      String[] splittedFilter = filter
        .split(RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR);

      for (int i = 0; i < items.getItemCount(); i++) {
        if (items.getItemText(i).equals(splittedFilter[0])) {
          items.setSelectedIndex(i);
        }
      }

      for (String fieldValue : fields.get(items.getSelectedValue())) {
        this.fields.addItem(fieldValue);
      }

      for (int i = 0; i < this.fields.getItemCount(); i++) {
        if (this.fields.getItemText(i).equals(splittedFilter[1])) {
          this.fields.setSelectedIndex(i);
        }
      }

      value.setText(splittedFilter[2]);
    } else {
      for (String fieldValue : fields.get(items.getSelectedValue())) {
        this.fields.addItem(fieldValue);
      }
    }
  }

  private void fillSelectBoxes(final Map<String, List<String>> mapping) {
    for (String field : mapping.keySet()) {
      items.addItem(field);
    }

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        RemovableRepresentationInformationFilter.this.fields.clear();
        for (String fieldValue : mapping.get(items.getSelectedValue())) {
          RemovableRepresentationInformationFilter.this.fields.addItem(fieldValue);
        }
      }
    };

    items.addChangeHandler(changeHandler);
  }

  public String getValue() {
    return items.getSelectedValue() + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR
      + fields.getSelectedValue() + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR
      + value.getValue();
  }

  public void addRemoveClickHandler(ClickHandler clickHandler) {
    removeDynamicButton.addClickHandler(clickHandler);
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }
}
