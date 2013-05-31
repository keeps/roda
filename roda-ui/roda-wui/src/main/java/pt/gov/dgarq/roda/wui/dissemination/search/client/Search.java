/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.client;

import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;
import pt.gov.dgarq.roda.wui.dissemination.search.advanced.client.AdvancedSearch;
import pt.gov.dgarq.roda.wui.dissemination.search.basic.client.BasicSearch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Search implements HistoryResolver {

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
			basicSearchLink = new Hyperlink("Pesquisa Básica",
					"dissemination.search.basic");
			advancedSearchLink = new Hyperlink("Pesquisa Avançada",
					"dissemination.search.advanced");

			layout.add(basicSearchLink);
			layout.add(advancedSearchLink);

		}
	}
	
	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRoles(
				new HistoryResolver[] { BasicSearch.getInstance(),
						AdvancedSearch.getInstance() }, false, callback);
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(layout);
		} else {
			if (historyTokens[0].equals(BasicSearch.getInstance()
					.getHistoryToken())) {
				BasicSearch.getInstance().resolve(Tools.tail(historyTokens),
						callback);
			} else if (historyTokens[0].equals(AdvancedSearch.getInstance()
					.getHistoryToken())) {
				AdvancedSearch.getInstance().resolve(Tools.tail(historyTokens),
						callback);
			} else {
				callback.onFailure(new BadHistoryTokenException(
						historyTokens[0]));
			}
		}
	}

	public String getHistoryPath() {
		return Dissemination.getInstance().getHistoryPath() + "."
				+ getHistoryToken();
	}

	public String getHistoryToken() {
		return "search";
	}

	

}
