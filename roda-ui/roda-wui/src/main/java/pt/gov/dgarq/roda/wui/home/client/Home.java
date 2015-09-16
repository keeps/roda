/**
 * 
 */
package pt.gov.dgarq.roda.wui.home.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;

/**
 * @author Luis Faria
 * 
 */
public class Home {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(Boolean.TRUE);
    }

    @Override
    public String getHistoryToken() {
      return "home";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  private static Home instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Home getInstance() {
    if (instance == null) {
      instance = new Home();
    }
    return instance;
  }

  private boolean initialized;

  private HTMLWidgetWrapper layout;

  private Home() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      layout = new HTMLWidgetWrapper("Home.html");
      layout.addStyleName("wui-home");
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(Home.RESOLVER);
      callback.onSuccess(null);
    }
  }

}
