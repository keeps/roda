/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.transfer;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.dialogs.SelectDialog;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RenameSelectDialog<T extends IsIndexed, O> extends DialogBox implements SelectDialog<T> {
  private static final Binder binder = GWT.create(Binder.class);

  @SuppressWarnings("rawtypes")
  interface Binder extends UiBinder<Widget, RenameSelectDialog> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox renameBox;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  String transferredResourceId;

  public RenameSelectDialog(String title, SelectedItems transferredResources) {

    if (transferredResources instanceof SelectedItemsList) {
      SelectedItemsList resourceList = (SelectedItemsList) transferredResources;
      transferredResourceId = (String) resourceList.getIds().get(0);
    } else {
      hide();
    }

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);
    renameBox.getElement().setPropertyString("placeholder", messages.renameSIPPlaceholder());

    center();
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
    BrowserService.Util.getInstance().renameTransferredResource(transferredResourceId, renameBox.getText(),
      new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
          hide();
        }

        @Override
        public void onSuccess(Void result) {
          Toast.showInfo(messages.dialogSuccess(), messages.renameSIPSuccessful());
          IngestTransfer.getInstance().refreshList();
          hide();
        }
      });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

}
