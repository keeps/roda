/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import config.i18n.client.MainMessages;
import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.dissemination.search.basic.client.BasicSearch;
import pt.gov.dgarq.roda.wui.home.client.Home;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;
import pt.gov.dgarq.roda.wui.management.client.Management;
import pt.gov.dgarq.roda.wui.management.user.client.Preferences;
import pt.gov.dgarq.roda.wui.management.user.client.RecoverLoginRequest;
import pt.gov.dgarq.roda.wui.management.user.client.Register;
import pt.gov.dgarq.roda.wui.management.user.client.ResetPassword;
import pt.gov.dgarq.roda.wui.management.user.client.VerifyEmail;

/**
 * @author Luis Faria
 * 
 */
public class ContentPanel extends SimplePanel {

	private static ContentPanel instance = null;

	private static ClientLogger logger = new ClientLogger(ContentPanel.class.getName());

	/**
	 * Get the singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static ContentPanel getInstance() {
		if (instance == null) {
			instance = new ContentPanel();
		}
		return instance;
	}

	private static final Set<HistoryResolver> resolvers = new HashSet<HistoryResolver>();

	private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

	private static MainMessages messages = (MainMessages) GWT.create(MainMessages.class);

	private Widget currWidget;

	private String currHistoryPath;

	private ContentPanel() {
		super();
		this.addStyleName("contentPanel");
		this.currWidget = null;

	}

	public void init() {
		// Home
		resolvers.add(Home.RESOLVER);
		// Browse
		resolvers.add(Browse.RESOLVER);
		// Search
		resolvers.add(BasicSearch.RESOLVER);
		// Ingest
		resolvers.add(Ingest.RESOLVER);
		// Management
		resolvers.add(Management.RESOLVER);
		// User Management
		resolvers.add(Preferences.getInstance());
		resolvers.add(RecoverLoginRequest.getInstance());
		resolvers.add(Register.getInstance());
		resolvers.add(ResetPassword.getInstance());
		resolvers.add(VerifyEmail.getInstance());
	}

	/**
	 * Update the content panel with the new history
	 * 
	 * @param historyTokens
	 *            the history tokens
	 */
	public void update(final String[] historyTokens) {
		boolean foundit = false;
		for (final HistoryResolver resolver : resolvers) {
			if (historyTokens[0].equals(resolver.getHistoryToken())) {
				foundit = true;
				currHistoryPath = Tools.join(historyTokens, ".");
				resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error resolving permissions", caught);
					}

					public void onSuccess(Boolean permitted) {
						if (!permitted.booleanValue()) {
							String windowLocation = Window.Location.getHref();
							CasForwardDialog cfd = new CasForwardDialog(windowLocation);
							cfd.show();
						} else {
							resolver.resolve(Tools.tail(historyTokens), new AsyncCallback<Widget>() {

								public void onFailure(Throwable caught) {
									if (caught instanceof BadHistoryTokenException) {
										Window.alert(messages.pageNotFound(caught.getMessage()));
										if (currWidget == null) {
											History.newItem(Home.RESOLVER.getHistoryPath());
										}
									}
								}

								public void onSuccess(Widget widget) {
									if (widget != null) {
										if (widget != currWidget) {
											currWidget = widget;
											setWidget(widget);
											logger.debug("reloaded content panel widget");
										}
										setWindowTitle(historyTokens);
									}
								}

							});
						}
					}

				});
			}
		}
		if (!foundit) {
			Window.alert(messages.pageNotFound(historyTokens[0]));
			if (currWidget == null) {
				History.newItem(Home.RESOLVER.getHistoryPath());
			} else {
				History.newItem(currHistoryPath);
			}
		}

	}

	private void setWindowTitle(String[] historyTokens) {
		String tokenI18N = "";
		boolean resolved = false;
		String[] tokens = historyTokens;
		while (!resolved && tokens.length > 0) {
			try {
				tokenI18N = constants.getString("title_" + Tools.join(tokens, "_")).toUpperCase();
				resolved = true;
			} catch (MissingResourceException e) {
				tokens = Tools.removeLast(tokens);
			}
		}
		if (!resolved) {
			tokenI18N = historyTokens[historyTokens.length - 1].toUpperCase();
		}

		// title.setText(tokenI18N);
		Window.setTitle(messages.windowTitle(tokenI18N));
	}

}
