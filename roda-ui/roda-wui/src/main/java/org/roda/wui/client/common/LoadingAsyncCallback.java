package org.roda.wui.client.common;

import org.roda.wui.client.common.utils.AsyncCallbackUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;

public abstract class LoadingAsyncCallback<T> implements AsyncCallback<T> {

  private DialogBox loadingModel;

  public LoadingAsyncCallback() {
    loadingModel = Dialogs.showLoadingModel();
  }

  @Override
  public void onFailure(Throwable caught) {
    loadingModel.hide();
    onFailureImpl(caught);
  }

  @Override
  public void onSuccess(T result) {
    loadingModel.hide();
    onSuccessImpl(result);
  }

  public void onFailureImpl(Throwable caught) {
    AsyncCallbackUtils.defaultFailureTreatment(caught);
  }

  public abstract void onSuccessImpl(T result);

}
