/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import pt.gov.dgarq.roda.wui.management.editor.client.MetadataEditor;
import pt.gov.dgarq.roda.wui.management.event.client.EventManagement;
import pt.gov.dgarq.roda.wui.management.statistics.client.Statistics;
import pt.gov.dgarq.roda.wui.management.user.client.MemberManagement;
import pt.gov.dgarq.roda.wui.management.user.client.UserLog;
import pt.gov.dgarq.roda.wui.management.user.client.WUIUserManagement;

/**
 * @author Luis Faria
 * 
 */
public class Management {

	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
			getInstance().resolve(historyTokens, callback);
		}

		@Override
		public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
			UserLogin.getInstance().checkRoles(new HistoryResolver[] { MemberManagement.RESOLVER,
					EventManagement.getInstance(), Statistics.getInstance(), UserLog.RESOLVER }, false, callback);
		}

		public String getHistoryPath() {
			return getHistoryToken();
		}

		public String getHistoryToken() {
			return "administration";
		}
	};

	private static Management instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static Management getInstance() {
		if (instance == null) {
			instance = new Management();
		}
		return instance;
	}

	private boolean initialized;

	private HTMLWidgetWrapper page;

	private HTMLWidgetWrapper help = null;

	private Management() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;
			page = new HTMLWidgetWrapper("Management.html");
		}
	}

	private HTMLWidgetWrapper getHelp() {
		if (help == null) {
			help = new HTMLWidgetWrapper("ManagementHelp.html");
		}
		return help;
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(page);
		} else {
			if (historyTokens[0].equals(MemberManagement.RESOLVER.getHistoryToken())) {
				MemberManagement.RESOLVER.resolve(Tools.tail(historyTokens), callback);
			} else if (historyTokens[0].equals(EventManagement.getInstance().getHistoryToken())) {
				EventManagement.getInstance().resolve(Tools.tail(historyTokens), callback);
			} else if (historyTokens[0].equals(MetadataEditor.getInstance().getHistoryToken())) {
				MetadataEditor.getInstance().resolve(Tools.tail(historyTokens), callback);
			} else if (historyTokens[0].equals(Statistics.getInstance().getHistoryToken())) {
				Statistics.getInstance().resolve(Tools.tail(historyTokens), callback);
			} else if (historyTokens[0].equals(UserLog.RESOLVER.getHistoryToken())) {
				UserLog.getInstance().resolve(Tools.tail(historyTokens), callback);
			} else if (historyTokens[0].equals("help")) {
				callback.onSuccess(getHelp());
			} else {
				callback.onFailure(new BadHistoryTokenException(historyTokens[0]));
			}
		}
	}
}
