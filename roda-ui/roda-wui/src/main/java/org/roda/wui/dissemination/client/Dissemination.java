/**
 * 
 */
package org.roda.wui.dissemination.client;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.dissemination.browse.client.Browse;
import org.roda.wui.dissemination.search.client.Search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationConstants;

/**
 * @author Luis Faria
 * 
 */
public class Dissemination {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    public String getHistoryToken() {
      return "dissemination";
    }
  };

  private static DisseminationConstants constants = (DisseminationConstants) GWT.create(DisseminationConstants.class);

  private static Dissemination instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Dissemination getInstance() {
    if (instance == null) {
      instance = new Dissemination();
    }
    return instance;
  }

  // private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

  private boolean initialized;

  private HTMLWidgetWrapper page;

  private HTMLWidgetWrapper help = null;

  private Dissemination() {
    initialized = false;

  }

  private void init() {
    if (!initialized) {
      initialized = true;
      page = new HTMLWidgetWrapper("Dissemination.html");
      page.addStyleName("wui-dissemination");
    }
  }

  private HTMLWidgetWrapper getHelp() {
    if (help == null) {
      help = new HTMLWidgetWrapper("DisseminationHelp.html");
    }
    return help;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(page);
    } else {
      if (historyTokens.get(0).equals(Search.RESOLVER.getHistoryToken())) {
        Search.getInstance().resolve(Tools.tail(historyTokens), callback);

      } else if (historyTokens.get(0).equals(Browse.RESOLVER.getHistoryToken())) {
        Browse.getInstance().resolve(Tools.tail(historyTokens), callback);

      } else if (historyTokens.get(0).equals("help")) {
        callback.onSuccess(getHelp());

      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    }
  }

  /**
   * Get translation of each descriptive level
   * 
   * @param level
   * @return the translation string
   */
  public String getElementLevelTranslation(DescriptionLevel level) {
    String ret;
    if (DescriptionLevelUtils.DESCRIPTION_LEVELS.contains(level)) {
      ret = constants.getString(level.getLevelSanitized());
    } else {
      ret = null;
    }
    return ret;
  }
}
