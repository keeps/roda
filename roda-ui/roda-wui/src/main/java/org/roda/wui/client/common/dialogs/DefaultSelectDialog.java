/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

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

public class DefaultSelectDialog<T extends IsIndexed> extends DialogBox implements SelectDialog<T> {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, DefaultSelectDialog> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  @UiField
  Button emptyParentButton;

  private final Class<T> objectClass;

  public DefaultSelectDialog(String title, ListBuilder<T> listBuilder) {
    objectClass = listBuilder.getOptions().getClassToReturn();
    GWT.log("-1");
    listBuilder.getOptions().withRecenteringOfParentDialog(this).addSelectionChangeHandler(event -> {
      T value = DefaultSelectDialog.this.getValue();
      selectButton.setEnabled(value != null);
    });

    searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("selectAipResultsPanel")
      .createListAndSearchPanel(listBuilder);

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);
    center();

    emptyParentButton.setVisible(false);
    selectButton.setEnabled(false);
  }

  public void setSingleSelectionMode() {
    selectButton.setEnabled(false);
  }

  @Override
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
    ValueChangeEvent.fire(this, getValue());
    hide();
  }

  @UiHandler("emptyParentButton")
  void buttonEmptyParentHandler(ClickEvent e) {
    ValueChangeEvent.fire(this, null);
    hide();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  private T getValue() {
    ListSelectionState<T> listSelectionState = searchWrapper.getListSelectionState(objectClass);
    return listSelectionState != null ? listSelectionState.getSelected() : null;
  }

  public void setEmptyParentButtonVisible(boolean visible) {
    emptyParentButton.setVisible(visible);
  }
}
