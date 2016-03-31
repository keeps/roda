/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
