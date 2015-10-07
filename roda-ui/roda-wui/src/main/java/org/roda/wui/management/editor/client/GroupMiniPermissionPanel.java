/**
 * 
 */
package org.roda.wui.management.editor.client;

import org.roda.core.data.v2.Group;
import org.roda.wui.management.user.client.GroupMiniPanel;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author Luis Faria
 * 
 */
public class GroupMiniPermissionPanel extends Composite {

  private final HorizontalPanel layout;
  private final GroupMiniPanel groupMiniPanel;
  private final ObjectPermissionsEditor objectPermissionsEditor;
  private final Group group;

  /**
   * Create a new group mini permission panel
   * 
   * @param group
   * @param metapermission
   */
  public GroupMiniPermissionPanel(Group group, ObjectPermissions metapermission) {
    this.layout = new HorizontalPanel();
    this.groupMiniPanel = new GroupMiniPanel(group.getName());
    this.group = group;
    objectPermissionsEditor = new ObjectPermissionsEditor(metapermission);

    layout.add(groupMiniPanel.getWidget());
    layout.add(objectPermissionsEditor);
    initWidget(layout);

    layout.setCellWidth(groupMiniPanel.getWidget(), "100%");

    layout.addStyleName("wui-group-mini-permissions");
    objectPermissionsEditor.addStyleName("wui-group-mini-permissions-editor");
  }

  /**
   * Get group
   * 
   * @return the group
   */
  public Group getGroup() {
    return group;
  }

  /**
   * Check if current panel is selected
   * 
   * @return true if selected
   */
  public boolean isSelected() {
    return groupMiniPanel.isSelected();
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
