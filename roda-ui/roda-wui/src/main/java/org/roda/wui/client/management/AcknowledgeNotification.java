/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class AcknowledgeNotification extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(Boolean.TRUE);
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "acknowledge";
    }
  };

  private static HTML acknowledgeBody;
  private static AcknowledgeNotification instance;

  private AcknowledgeNotification() {
    init();
  }

  public static AcknowledgeNotification getInstance() {
    if (instance == null) {
      instance = new AcknowledgeNotification();
    }
    return instance;
  }

  private static void init() {
    acknowledgeBody = new HTMLWidgetWrapper("Acknowledgements.html");
    acknowledgeBody.addStyleName("wui-home");
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 2) {
      final String notificationId = historyTokens.get(0);
      final String ackToken = historyTokens.get(1);

      BrowserService.Util.getInstance().acknowledgeNotification(notificationId, ackToken,
        new AsyncCallback<Notification>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(Notification notification) {
            callback.onSuccess(acknowledgeBody);
          }
        });
    } else {
      HistoryUtils.newHistory(NotificationRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

}
