/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.preingest.PreIngest;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.client.welcome.Help;
import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Luis Faria
 * 
 */
public class Ingest {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {PreIngest.RESOLVER, IngestTransfer.RESOLVER,
        IngestProcess.RESOLVER, IngestAppraisal.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "ingest";
    }
  };

  private static Ingest instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Ingest getInstance() {
    if (instance == null) {
      instance = new Ingest();
    }
    return instance;
  }

  private boolean initialized;
  private HTMLWidgetWrapper layout;
  private HTMLWidgetWrapper help = null;

  private Ingest() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      layout = new HTMLWidgetWrapper("Ingest.html");
    }
  }

  private HTMLWidgetWrapper getHelp() {
    if (help == null) {
      help = new HTMLWidgetWrapper("IngestHelp.html");
    }
    return help;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      init();
      callback.onSuccess(layout);
    } else {
      if (historyTokens.get(0).equals(PreIngest.RESOLVER.getHistoryToken())) {
        PreIngest.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(IngestTransfer.RESOLVER.getHistoryToken())) {
        IngestTransfer.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(IngestProcess.RESOLVER.getHistoryToken())) {
        IngestProcess.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(IngestAppraisal.RESOLVER.getHistoryToken())) {
        IngestAppraisal.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(Help.RESOLVER.getHistoryToken())) {
        callback.onSuccess(getHelp());
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    }
  }

  /**
   * Open new window to download RODA-in
   * 
   * @param targetUser
   *          the user for which to download the RODA-in Installer, or null to
   *          use the logged user
   * 
   * @param os
   *          the target operative system, e.g. windows, linux or mac. Use null
   *          to get a cross-platform installer
   */
  public static void downloadRodaIn(final User targetUser, final String os) {
    UserLogin.getRodaProperty("roda.in.installer.url", new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final String rodaInUrl) {
        UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(User user) {
            User target = targetUser == null ? user : targetUser;
            String url = rodaInUrl.replaceAll("$USERNAME", user.getName()) + "/" + target.getName();
            if (os != null) {
              url += "?os=" + os;
            }
            Window.open(url, "_blank", "");
          }

        });

      }

    });

  }

}
