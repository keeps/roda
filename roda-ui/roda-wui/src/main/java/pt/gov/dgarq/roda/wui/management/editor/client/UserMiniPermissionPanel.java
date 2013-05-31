/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.management.user.client.UserMiniPanel;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author Luis Faria
 * 
 */
public class UserMiniPermissionPanel extends Composite {

	private final HorizontalPanel layout;
	private final UserMiniPanel userMiniPanel;
	private final ObjectPermissionsEditor objectPermissionsEditor;

	/**
	 * Create a new user mini permission panel
	 * 
	 * @param user
	 * @param permissions
	 */
	public UserMiniPermissionPanel(User user, ObjectPermissions permissions) {
		layout = new HorizontalPanel();
		userMiniPanel = new UserMiniPanel(user);
		objectPermissionsEditor = new ObjectPermissionsEditor(permissions);
		layout.add(userMiniPanel.getWidget());
		layout.add(objectPermissionsEditor);

		initWidget(layout);

		layout.setCellWidth(userMiniPanel.getWidget(), "100%");

		layout.addStyleName("wui-user-mini-permissions");
		objectPermissionsEditor
				.addStyleName("wui-user-mini-permissions-editor");
	}

	/**
	 * Check if current panel is selected
	 * 
	 * @return true if selected
	 */
	public boolean isSelected() {
		return userMiniPanel.isSelected();
	}

	/**
	 * Get the user defined by this panel
	 * 
	 * @return the user
	 */
	public User getUser() {
		return userMiniPanel.getUser();
	}

	/**
	 * Get permissions
	 * 
	 * @return permissions
	 */
	public ObjectPermissions getPermissions() {
		return objectPermissionsEditor.getPermissions();
	}

	/**
	 * Add change listener
	 * 
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		objectPermissionsEditor.addChangeListener(listener);
	}

	/**
	 * Remove change listener
	 * 
	 * @param listener
	 */
	public void removeChangeListener(ChangeListener listener) {
		objectPermissionsEditor.removeChangeListener(listener);
	}

}
