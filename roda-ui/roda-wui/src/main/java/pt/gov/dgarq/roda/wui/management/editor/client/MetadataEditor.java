/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import pt.gov.dgarq.roda.wui.management.client.Management;

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

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(layout);
		} else {
			History.newItem(getHistoryPath());
			callback.onSuccess(null);
		}
	}

	public String getHistoryPath() {
		return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
	}

	public String getHistoryToken() {
		return "metadataEditor";
	}

}
