/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.pre.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;

/**
 * @author Luis Faria
 * 
 */
public class PreIngest {

	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
			getInstance().resolve(historyTokens, callback);
		}

		@Override
		public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
			UserLogin.getInstance().checkRole(this, callback);
		}

		@Override
		public String getHistoryToken() {
			return "pre";
		}

		@Override
		public String getHistoryPath() {
			return Ingest.RESOLVER.getHistoryPath() + "." + getHistoryToken();
		}
	};

	private static PreIngest instance = null;

	/**
	 * Get instance
	 * 
	 * @return {@link PreIngest} singleton
	 */
	public static PreIngest getInstance() {
		if (instance == null) {
			instance = new PreIngest();
		}
		return instance;
	}

	private VerticalPanel layout;

	private HTMLWidgetWrapper html;

	private PreIngest() {
		layout = new VerticalPanel();
		html = new HTMLWidgetWrapper("PreIngest.html");

		Filter classPlanFilter = new Filter();
		classPlanFilter.add(new ProducerFilterParameter());

		layout.add(html);

		layout.addStyleName("wui-ingest-pre");
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			callback.onSuccess(layout);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

}
