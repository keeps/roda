/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import com.google.gwt.user.client.Timer;
import org.roda.wui.client.management.RecoverLogin;
import org.roda.wui.client.management.Register;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.tools.HistoryUtils;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class ExpiredSessionDetector {
  private Timer timer;

  public ExpiredSessionDetector() {
    timer = new Timer() {
      @Override
      public void run() {
        checkSessionStatus();
      }
    };
  }

  public void setScheduleTime(int time) {
    timer.scheduleRepeating(time);
  }

  private boolean canGuestAccess() {
    String path = HistoryUtils.getCurrentHistoryPath().get(0);
    return path.equals(RecoverLogin.RESOLVER.getHistoryToken()) || path.equals(Login.RESOLVER.getHistoryToken())
      || path.equals(Theme.RESOLVER.getHistoryToken()) || path.equals(Register.RESOLVER.getHistoryToken())
      || path.equals(Welcome.RESOLVER.getHistoryToken());
  }

  public void checkSessionStatus() {
    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {
      @Override
      public void onFailure(Throwable caught) {
        UserLogin.getInstance().showSuggestLoginDialog();
      }

      @Override
      public void onSuccess(User user) {
        if (user.isGuest()) {
          if (!canGuestAccess()) {
            HistoryUtils.newHistory(Welcome.RESOLVER);
            UserLogin.getInstance().showSuggestLoginDialog();
          }
        }
      }
    }, true);
  }
}
