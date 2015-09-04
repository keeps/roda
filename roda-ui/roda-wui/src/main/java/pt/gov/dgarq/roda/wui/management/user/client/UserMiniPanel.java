package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import pt.gov.dgarq.roda.wui.management.user.client.images.UserManagementImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * User minimum info panel
 * 
 * @author Luis Faria
 */
public class UserMiniPanel {

	protected static UserManagementImageBundle userManagementImageBundle = (UserManagementImageBundle) GWT
			.create(UserManagementImageBundle.class);

	protected final User user;

	protected final AccessibleFocusPanel focus;

	protected final HorizontalPanel layout;

	protected Image userIcon;

	protected Label userNameLabel;

	protected boolean selected;

	protected List<ChangeListener> changeListeners;

	/**
	 * Create a new user panel
	 * 
	 * @param user
	 */
	public UserMiniPanel(User user) {
		this.user = user;
		focus = new AccessibleFocusPanel();
		layout = new HorizontalPanel();
		userIcon = user.isActive() ? userManagementImageBundle.user()
				.createImage() : userManagementImageBundle.inactiveUser()
				.createImage();
		userNameLabel = new Label(user.getFullName() + " (" + user.getName()
				+ ")");
		selected = false;
		changeListeners = new Vector<ChangeListener>();

		focus.setWidget(layout);
		layout.add(userIcon);
		layout.add(userNameLabel);

		focus.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				UserMiniPanel.this.setSelected(!selected);
				onChange();
			}

		});

		focus.setStylePrimaryName("wui-user-mini");
		layout.addStyleName("user-layout");
		userIcon.addStyleName("user-icon");
		userNameLabel.addStyleName("user-label");
	}

	/**
	 * Get the user panel widget
	 * 
	 * @return the widget
	 */
	public Widget getWidget() {
		return focus;
	}

	/**
	 * Get the user defined by this panel
	 * 
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Is this user selected
	 * 
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set this selected selected
	 * 
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected) {
			focus.addStyleDependentName("selected");
		} else {
			focus.removeStyleDependentName("selected");
		}
	}

	/**
	 * Add a change listener
	 * 
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Remove a change listener
	 * 
	 * @param listener
	 */
	public void removeChangeListenere(ChangeListener listener) {
		changeListeners.remove(listener);
	}

	protected void onChange() {
		for (ChangeListener listener : changeListeners) {
			listener.onChange(getWidget());
		}
	}
}