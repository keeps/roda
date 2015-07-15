/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.management.user.client.images.UserManagementImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class UserPanel implements SourcesClickEvents {

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

	private static UserManagementImageBundle userManagementImageBundle = (UserManagementImageBundle) GWT
			.create(UserManagementImageBundle.class);

	private final User user;

	private final FocusPanel focus;

	private final HorizontalPanel layout;

	private final Image icon;

	private final Label description;

	private final Image report;

	private ActionReportWindow actionReportWindow;

	private EditUser editUserPanel;

	private boolean selected;

	/**
	 * Create a new user panel
	 * 
	 * @param user
	 */
	public UserPanel(User user) {
		this.user = user;

		focus = new FocusPanel();
		layout = new HorizontalPanel();
		icon = user.isActive() ? userManagementImageBundle.user().createImage()
				: userManagementImageBundle.inactiveUser().createImage();
		description = new Label(user.getName() + " (" + user.getFullName() + ")");
		report = commonImageBundle.report().createImage();

		layout.add(icon);
		layout.add(description);
		layout.add(report);

		focus.setWidget(layout);

		report.addClickListener(new ClickListener() {

			public void onClick(Widget arg0) {
				showReport();
			}

		});

		focus.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				setSelected(!isSelected());
			}

		});

		actionReportWindow = null;
		editUserPanel = null;

		focus.setStylePrimaryName("wui-user-panel");
		if (!user.isActive()) {
			focus.addStyleDependentName("notActive");
		}
		layout.addStyleName("user-panel-layout");
		icon.addStyleName("user-panel-icon");
		description.addStyleName("user-panel-description");
		report.addStyleName("user-panel-report");

		layout.setCellWidth(description, "100%");

	}

	public void showReport() {
		if (actionReportWindow == null) {
			actionReportWindow = new ActionReportWindow(user);
		}
		actionReportWindow.show();
	}

	public void showEdit() {
		if (editUserPanel == null) {
			editUserPanel = new EditUser(user);
		}
		editUserPanel.show();
	}

	public Widget getWidget() {
		return focus;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected) {
			focus.addStyleDependentName("selected");
		} else {
			focus.removeStyleDependentName("selected");
		}
	}

	public void addClickListener(ClickListener listener) {
		focus.addClickListener(listener);
	}

	public void removeClickListener(ClickListener listener) {
		focus.removeClickListener(listener);
	}

	/**
	 * Is the user active
	 * 
	 * @return true if active, false otherwise
	 */
	public boolean isActive() {
		return user.isActive();
	}

	/**
	 * Set user active or inactive
	 * 
	 * @param active
	 *            true to set active, false to set inactive
	 * @param callback
	 *            handle operation finish
	 */
	public void setActive(boolean active, final AsyncCallback<Void> callback) {
		if (user.isActive() != active) {
			user.setActive(active);
			UserManagementService.Util.getInstance().editUser(user, null, new AsyncCallback<Void>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Void result) {
					if (isActive()) {
						focus.removeStyleDependentName("notActive");
					} else {
						focus.addStyleDependentName("notActive");
					}
					callback.onSuccess(null);
				}

			});
		} else {
			callback.onSuccess(null);
		}
	}

	public void remove(AsyncCallback<Boolean> callback) {
		UserManagementService.Util.getInstance().removeUser(user.getName(), callback);
	}

}
