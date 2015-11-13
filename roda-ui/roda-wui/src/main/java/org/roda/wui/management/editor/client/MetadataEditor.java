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
package org.roda.wui.management.editor.client;

import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.management.client.Management;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class MetadataEditor implements HistoryResolver {

  private static MetadataEditor instance = null;

  public static MetadataEditor getInstance() {
    if (instance == null) {
      instance = new MetadataEditor();
    }
    return instance;
  }

  // private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

  private boolean initialized;

  private HTMLWidgetWrapper layout;

  private MetadataEditor() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      layout = new HTMLWidgetWrapper("MetadataEditor.html");
    }
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().checkRole(this, callback);

  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(this);
      callback.onSuccess(null);
    }
  }

  public List<String> getHistoryPath() {
    return Tools.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
  }

  public String getHistoryToken() {
    return "metadataEditor";
  }

}
