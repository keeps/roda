/**
 * 
 */
package org.roda.wui.dissemination.search.client;

import java.util.List;

import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.dissemination.client.Dissemination;
import org.roda.wui.dissemination.search.basic.client.BasicSearch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Search {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "search";
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Dissemination.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static Search instance = null;

  public static Search getInstance() {
    if (instance == null) {
      instance = new Search();
    }
    return instance;
  }

  private boolean initialized;

  private VerticalPanel layout;

  private Hyperlink basicSearchLink;

  private Hyperlink advancedSearchLink;

  private Search() {
    initialized = false;

  }

  private void init() {
    if (!initialized) {
      initialized = true;

      layout = new VerticalPanel();
      basicSearchLink = new Hyperlink("Pesquisa Básica", "dissemination.search.basic");
      advancedSearchLink = new Hyperlink("Pesquisa Avançada", "dissemination.search.advanced");

      layout.add(basicSearchLink);
      layout.add(advancedSearchLink);

    }
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().checkRoles(new HistoryResolver[] {BasicSearch.RESOLVER}, false, callback);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(layout);
    } else {
      if (historyTokens.get(0).equals(BasicSearch.RESOLVER.getHistoryToken())) {
        BasicSearch.getInstance().resolve(Tools.tail(historyTokens), callback);
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    }
  }

}
