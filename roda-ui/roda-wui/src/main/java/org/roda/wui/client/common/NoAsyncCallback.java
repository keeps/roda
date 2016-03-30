package org.roda.wui.client.common;

import org.roda.wui.client.common.utils.AsyncCallbackUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class NoAsyncCallback<T> implements AsyncCallback<T> {

  @Override
  public void onFailure(Throwable caught) {
    AsyncCallbackUtils.defaultFailureTreatment(caught);
  }

  @Override
  public void onSuccess(T result) {
    // do nothing
  }

}
