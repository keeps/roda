/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import java.util.MissingResourceException;
import java.util.Stack;

import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class BreadcrumbPanel {

	// private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

	private static MainConstants mainConstants = (MainConstants) GWT
			.create(MainConstants.class);

	private final ContentPanel contentPanel;

	private String[] currentpath;

	private final Stack<Breadcrumb> breadcrumbs;

	private final HorizontalPanel layout;

	/**
	 * Create a new Breadcrumb panel
	 * 
	 * @param contentPanel
	 *            the content panel this breadcrumb panel will manage
	 */
	public BreadcrumbPanel(ContentPanel contentPanel) {
		super();
		this.contentPanel = contentPanel;
		this.breadcrumbs = new Stack<Breadcrumb>();
		this.layout = new HorizontalPanel();

		this.currentpath = null;

		UserLogin.getInstance().addLoginStatusListener(
				new LoginStatusListener() {

					public void onLoginStatusChanged(AuthenticatedUser user) {
						updatePath(currentpath);
					}

				});

		layout.addStyleName("wui-breadcrumbPanel");

	}

	protected Widget createSeparator() {
		final HTML separator = new HTML("&nbsp;/&nbsp;");
		separator.setStyleName("breadcrumb-separator");
		return separator;
	}

	/**
	 * Update the breadcrumb panel
	 * 
	 * @param path
	 *            the new history path
	 * 
	 */
	public void updatePath(String[] path) {

		// Check for common path
		int commonPathIndex = 0;
		boolean isCommonPath = true;
		int minLenght = Math.min(path.length, breadcrumbs.size());
		while (isCommonPath && commonPathIndex < minLenght) {
			String pathToken = path[commonPathIndex];
			Breadcrumb breadcrumb = (Breadcrumb) breadcrumbs
					.elementAt(commonPathIndex);
			if (pathToken.equals(breadcrumb.getLastToken())) {
				commonPathIndex++;
			} else {
				isCommonPath = false;
			}
		}

		// Pop path differences
		if (commonPathIndex == 0) {
			this.clear();
			breadcrumbs.clear();
		} else {
			int difference = breadcrumbs.size() - commonPathIndex;
			while (difference > 0) {
				this.breadcrumbs.pop();
				difference--;
			}
		}

		// Push the remain
		for (int i = commonPathIndex; i < path.length; i++) {
			String[] relativePath = new String[i + 1];
			for (int j = 0; j <= i; j++) {
				relativePath[j] = path[j];
			}
			push(relativePath);
		}

		updateLayout();

		// Refresh contentPanel
		contentPanel.update(path);
		currentpath = path;
	}

	protected void clear() {
		breadcrumbs.clear();
		updateLayout();
	}

	protected void updateLayout() {
		layout.clear();
		for (int i = 0; i < breadcrumbs.size(); i++) {
			if (i > 0) {
				layout.add(createSeparator());
			}
			Breadcrumb breadcrumb = (Breadcrumb) breadcrumbs.get(i);
			breadcrumb.setLast(i == breadcrumbs.size() - 1);
			layout.add(breadcrumb);
		}

	}

	protected void push(String[] path) {
		Breadcrumb breadcrumb = new Breadcrumb(path);
		breadcrumbs.add(breadcrumb);
	}

	/**
	 * Get the layout widget
	 * 
	 * @return the widget
	 */
	public Widget getWidget() {
		return layout;
	}

	protected class Breadcrumb extends Hyperlink {

		private String[] path;

		private boolean enabled;

		private boolean last;

		/**
		 * Create a new breadcrumb
		 * 
		 * @param path
		 *            the history path that this breadcrumb points to
		 */
		public Breadcrumb(String[] path) {
			super();
			super.setText(getText(path));
			super.setTargetHistoryToken(getTargetHistoryToken(path));
			this.path = path;
			enabled = true;
			last = true;

			addStyleName("breadcrumb");
		}

		/**
		 * Is this breadcrumb enabled
		 * 
		 * @return true if enabled
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * Set if this breadcrumb is enabled. Overrides the click event
		 * 
		 * @param enabled
		 * 
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * Is this the last breadcrumb in list (current active breadcrumb)
		 * 
		 * @return true if this breadcrumb is the last one. The last breadcrumb
		 *         is disabled and has a different style.
		 */
		public boolean isLast() {
			return last;
		}

		/**
		 * Get the history path defined by this breadcrumb
		 * 
		 * @return the breadcrumb history path
		 */
		public String[] getPath() {
			return path;
		}

		/**
		 * Get the last token on the history path defined by this breadcrumb
		 * 
		 * @return the last history token
		 */
		public String getLastToken() {
			return path[path.length - 1];
		}

		/**
		 * Set if this breadcrumb is the last one. The last breadcrumb will be
		 * disabled and has a different style.
		 * 
		 * @param last
		 */
		public void setLast(boolean last) {
			this.last = last;
			this.setEnabled(!last);
			if (last) {
				this.addStyleName("breadcrumb-last");
			} else {
				this.removeStyleName("breadcrumb-last");
			}
		}

		public void onBrowserEvent(final Event event) {
			if (enabled) {
				super.onBrowserEvent(event);
			}
		}

		protected String getText(String[] path) {
			String tokenI18N;
			try {
				tokenI18N = mainConstants.getString(
						"title_" + Tools.join(path, "_"))
						.toLowerCase();
			} catch (MissingResourceException e) {
				tokenI18N = path[path.length - 1].toLowerCase();
			}
			return tokenI18N;
		}

		protected String getTargetHistoryToken(String[] path) {
			return Tools.join(path, ".");
		}

	}
}
