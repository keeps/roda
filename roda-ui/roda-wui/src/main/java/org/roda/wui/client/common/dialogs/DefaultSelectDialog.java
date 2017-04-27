/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.search.SearchPanel;

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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

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

  public DefaultSelectDialog(String title, Filter filter, String searchField, AsyncTableCell<T, O> searchResultsPanel,
    boolean hidePreFilters) {
    this.searchResultsPanel = searchResultsPanel;

    searchPanel = new SearchPanel(filter, searchField, true, messages.selectAipSearchPlaceHolder(), false, false,
      hidePreFilters);
    searchPanel.setList(searchResultsPanel);

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);
    center();

    emptyParentButton.setVisible(false);
    selectButton.setEnabled(false);

    this.searchResultsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        selectButton.setEnabled(DefaultSelectDialog.this.getValue() != null);
      }
    });
  }

  @Override
  public void showAndCenter() {
    if (Window.getClientWidth() < 800) {
      this.setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  public void setSingleSelectionMode() {
    selectButton.setEnabled(false);

    searchResultsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        if (DefaultSelectDialog.this.getValue() != null) {
          onChange();
          hide();
        }
      }
    });
  }

  public void hidePreFilters() {
    searchPanel.hidePreFilters();
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

  public BasicAsyncTableCell<T> getList() {
    return (BasicAsyncTableCell<T>) searchResultsPanel;
  }

  public void setEmptyParentButtonVisible(boolean visible) {
    emptyParentButton.setVisible(visible);
  }
}
