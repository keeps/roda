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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.browse.tabs.BrowseNotificationsTabs;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
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

  interface MyUiBinder extends UiBinder<Widget, ShowNotification> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.NOTIFICATION_ID, RodaConstants.NOTIFICATION_SUBJECT, RodaConstants.NOTIFICATION_BODY,
    RodaConstants.NOTIFICATION_SENT_ON, RodaConstants.NOTIFICATION_FROM_USER,
    RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED, RodaConstants.NOTIFICATION_RECIPIENT_USERS,
    RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS, RodaConstants.NOTIFICATION_STATE);

  @UiField
  TitlePanel title;
  @UiField
  ActionsToolbar actionsToolbar;
  @UiField
  NavigationToolbar<Notification> navigationToolbar;
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  BrowseNotificationsTabs browseTab;

  public ShowNotification(Notification notification) {
    initWidget(uiBinder.createAndBindUi(this));
    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getNotificationBreadcrumbs(notification));

    actionsToolbar.setLabel(messages.notificationTitle());
    actionsToolbar.setTagsVisible(false);

    title.setText(StringUtils.isNotBlank(notification.getSubject()) ? notification.getSubject() : notification.getId());

    browseTab.init(notification);

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse browse-file browse_main_panel");
  }

}
