package pt.gov.dgarq.roda.wui.ingest.client;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestList;
import pt.gov.dgarq.roda.wui.ingest.pre.client.PreIngest;
import pt.gov.dgarq.roda.wui.ingest.submit.client.IngestSubmit;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Luis Faria
 * 
 */
public class Ingest implements HistoryResolver {

	private static Ingest instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static Ingest getInstance() {
		if (instance == null) {
			instance = new Ingest();
		}
		return instance;
	}

	private static ClientLogger logger = new ClientLogger(Ingest.class
			.getName());

	private boolean initialized;

	private HTMLWidgetWrapper layout;

	private HTMLWidgetWrapper help = null;

	private Ingest() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;
			layout = new HTMLWidgetWrapper("Ingest.html");
		}
	}

	private HTMLWidgetWrapper getHelp() {
		if (help == null) {
			help = new HTMLWidgetWrapper("IngestHelp.html");
		}
		return help;
	}

	public String getHistoryPath() {
		return getHistoryToken();
	}

	public String getHistoryToken() {
		return "ingest";
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRoles(
				new HistoryResolver[] { PreIngest.getInstance(),
						IngestSubmit.getInstance(), IngestList.getInstance() },
				false, callback);
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(layout);
		} else {
			if (historyTokens[0].equals(PreIngest.getInstance()
					.getHistoryToken())) {
				PreIngest.getInstance().resolve(Tools.tail(historyTokens),
						callback);
			} else if (historyTokens[0].equals(IngestSubmit.getInstance()
					.getHistoryToken())) {
				IngestSubmit.getInstance().resolve(Tools.tail(historyTokens),
						callback);
			} else if (historyTokens[0].equals(IngestList.getInstance()
					.getHistoryToken())) {
				IngestList.getInstance().resolve(Tools.tail(historyTokens),
						callback);
			} else if (historyTokens[0].equals("help")) {
				callback.onSuccess(getHelp());
			} else {
				callback.onFailure(new BadHistoryTokenException(
						historyTokens[0]));
			}
		}
	}

	/**
	 * Open new window to download RODA-in
	 * 
	 * @param targetUser
	 *            the user for which to download the RODA-in Installer, or null
	 *            to use the logged user
	 * 
	 * @param os
	 *            the target operative system, e.g. windows, linux or mac. Use
	 *            null to get a cross-platform installer
	 */
	public static void downloadRodaIn(final User targetUser, final String os) {
		UserLogin.getRodaProperty("roda.in.installer.url",
				new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting RODA-in", caught);
					}

					public void onSuccess(final String rodaInUrl) {
						UserLogin.getInstance().getAuthenticatedUser(
								new AsyncCallback<AuthenticatedUser>() {

									public void onFailure(Throwable caught) {
										logger.error("Error getting RODA-in",
												caught);
									}

									public void onSuccess(AuthenticatedUser user) {
										User target = targetUser == null ? user
												: targetUser;
										String url = rodaInUrl.replaceAll(
												"$USERNAME", user.getName())
												+ "/" + target.getName();
										if (os != null) {
											url += "?os=" + os;
										}
										Window.open(url, "_blank", "");

									}

								});

					}

				});

	}

}
