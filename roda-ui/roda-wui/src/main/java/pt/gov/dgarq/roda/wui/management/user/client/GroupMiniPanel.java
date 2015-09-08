/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import pt.gov.dgarq.roda.wui.management.user.client.images.UserManagementImageBundle;

/**
 * @author Luis Faria
 * 
 */
public class GroupMiniPanel {
	private static UserManagementImageBundle userManagementImageBundle = (UserManagementImageBundle) GWT
			.create(UserManagementImageBundle.class);

	protected final String groupName;

	protected final AccessibleFocusPanel focus;

	protected final HorizontalPanel layout;

	protected final Image groupIcon;

	protected final Label groupNameLabel;

	protected boolean selected;

	protected List<ChangeListener> changeListeners;

	/**
	 * Group panel constructor
	 * 
	 * @param groupName
	 *            the group name
	 */
	public GroupMiniPanel(String groupName) {
		this.groupName = groupName;
		focus = new AccessibleFocusPanel();
		layout = new HorizontalPanel();
		groupIcon = userManagementImageBundle.group().createImage();
		groupNameLabel = new Label(groupName);
		selected = false;
		changeListeners = new Vector<ChangeListener>();

		focus.setWidget(layout);
		layout.add(groupIcon);
		layout.add(groupNameLabel);

		focus.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				GroupMiniPanel.this.setSelected(!selected);
				onChange();
			}

		});

		focus.setStylePrimaryName("wui-group-mini");
		layout.addStyleName("group-layout");
		groupIcon.addStyleName("group-icon");
		groupNameLabel.addStyleName("group-label");
	}

	/**
	 * Get the group panel widget
	 * 
	 * @return the widget
	 */
	public Widget getWidget() {
		return focus;
	}

	/**
	 * Get the group name
	 * 
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Get the group defined by this group panel
	 * 
	 * @param callback
	 *            the callback to handle the asynchronously returned group
	 */
	public void getGroup(AsyncCallback<RodaGroup> callback) {
		UserManagementService.Util.getInstance().getGroup(groupName, callback);
	}

	/**
	 * Is this group selected
	 * 
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set this group selected
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
