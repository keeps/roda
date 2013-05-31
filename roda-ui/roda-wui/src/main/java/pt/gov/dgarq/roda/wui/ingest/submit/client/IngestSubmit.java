/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.submit.client;

import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestSubmitConstants;

/**
 * @author Luis Faria
 * 
 */
public class IngestSubmit implements HistoryResolver {
	private static IngestSubmit instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return {@link IngestSubmit}
	 */
	public static IngestSubmit getInstance() {
		if (instance == null) {
			instance = new IngestSubmit();
		}
		return instance;
	}

	private static IngestSubmitConstants constants = (IngestSubmitConstants) GWT
			.create(IngestSubmitConstants.class);

	private boolean initialized;

	private TabPanel layout;

	private UploadSIP uploadSIP;

	private CreateSIP createSIP;

	private IngestSubmit() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;

			layout = new TabPanel();
			uploadSIP = new UploadSIP();
			createSIP = new CreateSIP();
			layout.add(uploadSIP.getWidget(), constants.uploadTabTitle());
			layout.add(createSIP.getWidget(), constants.createTabTitle());

			layout.addTabListener(new TabListener() {

				public boolean onBeforeTabSelected(SourcesTabEvents sender,
						int tabIndex) {
					switch (tabIndex) {
					case 1:
						History.newItem(getHistoryPath() + ".create");
						break;
					case 0:
					default:
						History.newItem(getHistoryPath() + ".upload");
						break;
					}
					return true;
				}

				public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
					// nothing to do

				}

			});

			layout.addStyleName("wui-ingest-submit");
		}
	}

	public String getHistoryPath() {
		return Ingest.getInstance().getHistoryPath() + "." + getHistoryToken();
	}

	public String getHistoryToken() {
		return "submit";
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRole(this, callback);

	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		String defaultHistoryPath = getHistoryPath() + ".upload";
		if (historyTokens.length == 0) {
			History.newItem(defaultHistoryPath);
			callback.onSuccess(null);
		} else if (historyTokens.length == 1) {
			if (historyTokens[0].equals("upload")) {
				init();
				uploadSIP.init();
				layout.selectTab(0);
				callback.onSuccess(layout);
			} else if (historyTokens[0].equals("create")) {
				init();
				createSIP.init();
				layout.selectTab(1);
				callback.onSuccess(layout);
			} else {
				callback.onFailure(new BadHistoryTokenException(
						historyTokens[0]));
			}
		} else {
			History.newItem(defaultHistoryPath);
			callback.onSuccess(null);
		}
	}

}
