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

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.Window;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Theme extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String token : historyTokens) {
          if (first) {
            first = false;
          } else {
            sb.append("/");
          }
          sb.append(token);
        }
        Theme theme = new Theme(sb.toString());
        callback.onSuccess(theme);
      } else {
        HistoryUtils.newHistory(Theme.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(Boolean.TRUE);
    }

    @Override
    public String getHistoryToken() {
      return "theme";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  private HTMLWidgetWrapper layout;

  private Theme(String htmlPage) {
    layout = new HTMLWidgetWrapper(htmlPage, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError(caught);
      }

      @Override
      public void onSuccess(Void result) {
        JavascriptUtils.runHighlighter();
        JavascriptUtils.runTextFill(".textfill");
        JavascriptUtils.scrollToTop();
      }
    });
    layout.addStyleName("wui-home");
    initWidget(layout);
  }

}
