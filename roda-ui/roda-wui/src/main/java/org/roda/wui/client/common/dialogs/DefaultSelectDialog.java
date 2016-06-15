/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.lists.AsyncTableCell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DefaultSelectDialog<T extends IsIndexed, O> extends DialogBox implements SelectDialog<T> {
  private static final Binder binder = GWT.create(Binder.class);

  @SuppressWarnings("rawtypes")
  interface Binder extends UiBinder<Widget, DefaultSelectDialog> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  @UiField
  Button emptyParentButton;

  @UiField(provided = true)
  AsyncTableCell<T, O> searchResultsPanel;

  public DefaultSelectDialog(String title, Filter filter, String searchField, AsyncTableCell<T, O> searchResultsPanel) {
    this.searchResultsPanel = searchResultsPanel;

    searchPanel = new SearchPanel(filter, searchField, messages.selectAipSearchPlaceHolder(), false, false);
    searchPanel.setList(searchResultsPanel);
    searchPanel.setDefaultFilterIncremental(true);

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);

    center();

    emptyParentButton.setVisible(false);
  }

  public void showAndCenter() {
    if (Window.getClientWidth() < 800) {
      this.setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  @UiHandler("cancelButton")
  void buttonCancelHandler(ClickEvent e) {
    hide();
  }

  @UiHandler("selectButton")
  void buttonSelectHandler(ClickEvent e) {
    onChange();
    hide();
  }

  @UiHandler("emptyParentButton")
  void buttonEmptyParentHandler(ClickEvent e) {
    searchResultsPanel.getSelectionModel().clear();
    onChange();
    hide();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public T getValue() {
    return searchResultsPanel.getSelectionModel().getSelectedObject();
  }

  public void setEmptyParentButtonVisible(boolean visible) {
    emptyParentButton.setVisible(visible);
  }
}
