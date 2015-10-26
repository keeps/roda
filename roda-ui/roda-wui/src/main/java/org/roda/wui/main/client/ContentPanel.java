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
package org.roda.wui.main.client;

import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.dissemination.browse.client.Browse;
import org.roda.wui.dissemination.search.basic.client.BasicSearch;
import org.roda.wui.home.client.Home;
import org.roda.wui.ingest.client.Ingest;
import org.roda.wui.management.client.Management;
import org.roda.wui.management.user.client.Preferences;
import org.roda.wui.management.user.client.RecoverLoginRequest;
import org.roda.wui.management.user.client.Register;
import org.roda.wui.management.user.client.ResetPassword;
import org.roda.wui.management.user.client.VerifyEmail;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import config.i18n.client.MainMessages;

/**
 * @author Luis Faria
 * 
 */
public class ContentPanel extends SimplePanel {

  private static ContentPanel instance = null;

  private static ClientLogger logger = new ClientLogger(ContentPanel.class.getName());

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

  private static final Set<HistoryResolver> resolvers = new HashSet<HistoryResolver>();

  private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

  private static MainMessages messages = (MainMessages) GWT.create(MainMessages.class);

  private Widget currWidget;

  private List<String> currHistoryPath;

  private ContentPanel() {
    super();
    this.addStyleName("contentPanel");
    this.currWidget = null;

  }

  public void init() {
    // Login
    resolvers.add(Login.RESOLVER);
    // Home
    resolvers.add(Home.RESOLVER);
    // Browse
    resolvers.add(Browse.RESOLVER);
    // Search
    resolvers.add(BasicSearch.RESOLVER);
    // Ingest
    resolvers.add(Ingest.RESOLVER);
    // Management
    resolvers.add(Management.RESOLVER);
    // User Management
    resolvers.add(Preferences.getInstance());
    resolvers.add(RecoverLoginRequest.getInstance());
    resolvers.add(Register.getInstance());
    resolvers.add(ResetPassword.getInstance());
    resolvers.add(VerifyEmail.getInstance());
  }

  /**
   * Update the content panel with the new history
   * 
   * @param historyTokens
   *          the history tokens
   */
  public void update(final List<String> historyTokens) {
    boolean foundit = false;
    for (final HistoryResolver resolver : resolvers) {
      if (historyTokens.get(0).equals(resolver.getHistoryToken())) {
        foundit = true;
        currHistoryPath = historyTokens;
        resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            logger.error("Error resolving permissions", caught);
          }

          public void onSuccess(Boolean permitted) {
            if (!permitted.booleanValue()) {
              String windowLocation = Window.Location.getHref();
              CasForwardDialog cfd = new CasForwardDialog(windowLocation);
              cfd.show();
            } else {
              resolver.resolve(Tools.tail(historyTokens), new AsyncCallback<Widget>() {

                public void onFailure(Throwable caught) {
                  if (caught instanceof BadHistoryTokenException) {
                    Window.alert(messages.pageNotFound(caught.getMessage()));
                    if (currWidget == null) {
                      Tools.newHistory(Home.RESOLVER);
                    }
                  }
                }

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
    }
    if (!foundit) {
      Window.alert(messages.pageNotFound(historyTokens.get(0)));
      if (currWidget == null) {
        Tools.newHistory(Home.RESOLVER);
      } else {
        Tools.newHistory(currHistoryPath);
      }
    }

  }

  private void setWindowTitle(List<String> historyTokens) {
    String tokenI18N = "";
    boolean resolved = false;
    List<String> tokens = historyTokens;
    while (!resolved && tokens.size() > 0) {
      try {
        tokenI18N = constants.getString("title_" + Tools.join(tokens, "_")).toUpperCase();
        resolved = true;
      } catch (MissingResourceException e) {
        tokens = Tools.removeLast(tokens);
      }
    }
    if (!resolved) {
      tokenI18N = historyTokens.get(historyTokens.size() - 1).toUpperCase();
    }

    // title.setText(tokenI18N);
    Window.setTitle(messages.windowTitle(tokenI18N));
  }

}
