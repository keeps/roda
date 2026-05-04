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

import java.util.List;

import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.browse.tabs.NotificationsTabs;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowNotification extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String notificationId = historyTokens.get(0);
        Services services = new Services("Get notification", "get");
        services.notificationResource(s -> s.getNotification(notificationId))
          .whenComplete((notification, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else {
              ShowNotification notificationPanel = new ShowNotification(notification);
              callback.onSuccess(notificationPanel);
            }
          });
      } else {
        HistoryUtils.newHistory(NotificationRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(NotificationRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "notification";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  TitlePanel title;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  NavigationToolbar<Notification> navigationToolbar;
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NotificationsTabs browseTab;

  public ShowNotification(Notification notification) {
    initWidget(uiBinder.createAndBindUi(this));
    navigationToolbar.withObject(notification).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getNotificationBreadcrumbs(notification));

    actionsToolbar.setLabel(messages.notificationTitle());

    title.setText(StringUtils.isNotBlank(notification.getSubject()) ? notification.getSubject() : notification.getId());

    browseTab.init(notification);

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse browse-file browse_main_panel");
  }

  interface MyUiBinder extends UiBinder<Widget, ShowNotification> {
  }
}
