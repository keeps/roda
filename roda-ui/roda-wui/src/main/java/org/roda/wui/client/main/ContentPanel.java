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
package org.roda.wui.client.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.management.AcknowledgeNotification;
import org.roda.wui.client.management.Management;
import org.roda.wui.client.management.Profile;
import org.roda.wui.client.management.RecoverLogin;
import org.roda.wui.client.management.Register;
import org.roda.wui.client.management.ResetPassword;
import org.roda.wui.client.management.VerifyEmail;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.process.Process;
import org.roda.wui.client.search.Relation;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.welcome.Help;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ContentPanel extends SimplePanel {

  private static ContentPanel instance = null;

  @SuppressWarnings("unused")
  private static ClientLogger logger = new ClientLogger(ContentPanel.class.getName());

  private static final Set<HistoryResolver> resolvers = new HashSet<>();
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private Widget currWidget;

  private ContentPanel() {
    super();
    this.addStyleName("contentPanel");
    this.currWidget = null;
  }

  /**
   * Get the singleton instance
   * 
   * @return the singleton instance
   */
  public static ContentPanel getInstance() {
    if (instance == null) {
      instance = new ContentPanel();
    }
    return instance;
  }

  public void init() {
    // Login
    resolvers.add(Login.RESOLVER);
    // Home
    resolvers.add(Welcome.RESOLVER);
    // Theme static pages
    resolvers.add(Theme.RESOLVER);
    // Browse
    resolvers.add(BrowseAIP.RESOLVER);
    // Search
    resolvers.add(Search.RESOLVER);
    resolvers.add(Relation.RESOLVER);
    // Ingest
    resolvers.add(Ingest.RESOLVER);
    // Management
    resolvers.add(Management.RESOLVER);
    // Planning
    resolvers.add(Planning.RESOLVER);
    // User Management
    resolvers.add(Profile.RESOLVER);
    resolvers.add(Register.RESOLVER);
    resolvers.add(RecoverLogin.RESOLVER);
    resolvers.add(ResetPassword.RESOLVER);
    resolvers.add(VerifyEmail.RESOLVER);
    resolvers.add(Process.RESOLVER);

    // Help
    resolvers.add(Help.RESOLVER);

    // UUID resolver
    resolvers.add(HistoryUtils.UUID_RESOLVER);

    // Acknowlege page
    resolvers.add(AcknowledgeNotification.RESOLVER);
  }

  /**
   * Update the content panel with the new history
   * 
   * @param historyTokens
   *          the history tokens
   */
  public void update(final List<String> historyTokens) {
    HistoryResolver foundResolver = null;
    for (final HistoryResolver resolver : resolvers) {
      if (historyTokens.get(0).equals(resolver.getHistoryToken())) {
        foundResolver = resolver;
        break;
      }
    }

    if (foundResolver != null) {
      update(historyTokens, foundResolver);
    } else {
      HistoryUtils.newHistory(Theme.RESOLVER, "Error404.html");
    }
  }

  private void update(final List<String> historyTokens, final HistoryResolver resolver) {
    resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Boolean permitted) {
        if (!permitted.booleanValue()) {
          UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {
            @Override
            public void onFailure(Throwable caught) {
              UserLogin.getInstance().showSuggestLoginDialog();
            }

            @Override
            public void onSuccess(User user) {
              if (user.isGuest()) {
                UserLogin.getInstance().showSuggestLoginDialog();
              } else {
                Dialogs.showInformationDialog(messages.authorizationDeniedAlert(),
                  messages.authorizationDeniedAlertMessageExceptionSimple(), messages.dialogOk(),
                  new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      HistoryUtils.newHistory(Welcome.RESOLVER);
                    }

                    @Override
                    public void onSuccess(Void result) {
                      HistoryUtils.newHistory(Welcome.RESOLVER);
                    }
                  });
              }
            }
          });
        } else {
          resolver.resolve(HistoryUtils.tail(historyTokens), new AsyncCallback<Widget>() {

            @Override
            public void onFailure(Throwable caught) {
              if (caught instanceof BadHistoryTokenException) {
                HistoryUtils.newHistory(Theme.RESOLVER, "Error404.html");
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            }

            @Override
            public void onSuccess(Widget widget) {
              if (widget != null) {
                if (widget != currWidget) {
                  currWidget = widget;
                  setWidget(widget);
                }
                setWindowTitle(historyTokens);
              }
            }

          });
        }
      }

    });
  }

  private void setWindowTitle(List<String> historyTokens) {
    String tokenI18N = "";
    boolean resolved = false;
    List<String> tokens = historyTokens;

    while (!resolved && !tokens.isEmpty()) {
      String token = StringUtils.join(tokens, "_");
      tokenI18N = messages.title(token).toUpperCase();

      if (tokenI18N.isEmpty()) {
        tokens = HistoryUtils.removeLast(tokens);
      } else {
        resolved = true;
      }

    }

    if (!resolved) {
      String lastToken = historyTokens.get(historyTokens.size() - 1);

      // TODO generalize suffix approach
      if (lastToken.endsWith(".html")) {
        lastToken = lastToken.substring(0, lastToken.length() - ".html".length());
      }

      // transform camel case to spaces
      lastToken = lastToken.replaceAll("([A-Z])", " $1");

      // upper-case
      lastToken = lastToken.toUpperCase();
      tokenI18N = lastToken;
    }

    // title.setText(tokenI18N);
    Window.setTitle(messages.windowTitle(tokenI18N));
  }

}
