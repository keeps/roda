/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package org.roda.wui.client.common;

import java.util.function.Consumer;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.tools.HistoryUtils;

/**
 * Asynchronous callback with a default failure handler
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class DefaultMethodCallback<T> implements MethodCallback<T> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static <T> MethodCallback<T> get(final Consumer<T> consumer) {
    return new DefaultMethodCallback<T>() {
      @Override
      public void onFailure(Method method, Throwable throwable) {
        if (throwable instanceof NotFoundException) {
          Dialogs.showInformationDialog(messages.ingestTransferNotFoundDialogTitle(),
            messages.ingestTransferNotFoundDialogMessage(), messages.ingestTransferNotFoundDialogButton(),
            false, new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(Void result) {
                HistoryUtils.newHistory(IngestTransfer.RESOLVER);
              }
            });
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
          HistoryUtils.newHistory(IngestTransfer.RESOLVER);
        }
      }
      @Override
      public void onSuccess(Method method, T t) {
        consumer.accept(t);
      }
    };
  }

  public static <T> MethodCallback<T> get(final Consumer<T> consumer, final Consumer<String> errorHandler) {
    return new MethodCallback<T>() {
      @Override
      public void onFailure(Method method, Throwable throwable) {
        final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());
        String message = parse.isObject().get("message").isString().stringValue();
        errorHandler.accept(message);
      }

      @Override
      public void onSuccess(Method method, T t) {
        consumer.accept(t);
      }
    };
  }

  @Override
  public void onFailure(Method method, Throwable throwable) {
    String message;
    if (method.getResponse().getText() != "") {
      message = throwable.getMessage();
    } else {
      final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());
      message = parse.isObject().get("message").isString().stringValue();
    }

    // TODO resolve specific exceptions
    if (method.getResponse().getStatusCode() == Response.SC_UNAUTHORIZED) {
      // TODO open dialog to states that is unauthorized and ask to login if currently
      // not logged in (guest) or to ask the administrator to add permissions to your
      // user.
      GWT.log(message);

    } else if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      GWT.log(message);
    }
  }

  public static <T> MethodCallback<T> get(final AsyncCallback<T> callback) {
    return new MethodCallback<T>() {

      @Override
      public void onFailure(Method method, Throwable throwable) {
        callback.onFailure(throwable);
      }

      @Override
      public void onSuccess(Method method, T t) {
          callback.onSuccess(t);
      }
    };

  }

}
