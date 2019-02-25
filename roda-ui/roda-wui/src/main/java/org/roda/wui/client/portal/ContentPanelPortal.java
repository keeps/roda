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
package org.roda.wui.client.portal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.main.Theme;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class ContentPanelPortal extends SimplePanel {
  private static ContentPanelPortal instance = null;

  private static final Set<HistoryResolver> resolvers = new HashSet<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Widget currWidget;
  private List<String> lastHistoryTokens = null;
  private HistoryResolver lastResolver = null;

  private ContentPanelPortal() {
    super();
    this.addStyleName("contentPanel");
    this.currWidget = null;
  }

  /**
   * Get the singleton instance
   * 
   * @return the singleton instance
   */
  public static ContentPanelPortal getInstance() {
    if (instance == null) {
      instance = new ContentPanelPortal();
    }
    return instance;
  }

  public void init() {
    // Login
    resolvers.add(Login.RESOLVER);
    // Theme static pages
    resolvers.add(Theme.RESOLVER);
    // Browse
    resolvers.add(BrowseAIPPortal.RESOLVER);
    // Search
    resolvers.add(SearchPortal.RESOLVER);
    // UUID resolver
    resolvers.add(HistoryUtils.UUID_RESOLVER);
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
        if (!permitted) {
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
                  messages.authorizationDeniedAlertMessageExceptionSimple(""), messages.dialogOk(),
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
                  ContentPanelPortal.this.lastHistoryTokens = historyTokens;
                  ContentPanelPortal.this.lastResolver = resolver;
                  setWidget(widget);
                  JavascriptUtils.scrollToTop();
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
