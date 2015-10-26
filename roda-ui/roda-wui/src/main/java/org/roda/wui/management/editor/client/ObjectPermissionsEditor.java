/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.editor.client;

import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class ObjectPermissionsEditor extends ListBox {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private ObjectPermissions permissions;
  private ObjectPermissions originalPermissions;

  /**
   * Create a new object permissions editor
   * 
   * @param permissions
   */
  public ObjectPermissionsEditor(ObjectPermissions permissions) {
    super();
    int index = 0;
    int selectedIndex = -1;
    for (ObjectPermissions permission : ObjectPermissions.values()) {
      addItem(toString(permission));
      if (permission.equals(permissions)) {
        selectedIndex = index;
      }
      index++;
    }

    if (selectedIndex >= 0) {
      setSelectedIndex(selectedIndex);
    } else {
      addItem(toString(permissions));
      setSelectedIndex(index);
    }

    this.permissions = permissions;
    this.originalPermissions = permissions;

    addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        if (ObjectPermissions.values().length > getSelectedIndex()) {
          ObjectPermissionsEditor.this.permissions = ObjectPermissions.values()[getSelectedIndex()];
        } else {
          ObjectPermissionsEditor.this.permissions = originalPermissions;
        }
      }

    });

  }

  private String toString(ObjectPermissions permissions) {
    String ret;
    if (permissions.equals(ObjectPermissions.NoAccess)) {
      ret = constants.permission_object_NoAccess();
    } else if (permissions.equals(ObjectPermissions.ReadOnly)) {
      ret = constants.permission_object_ReadOnly();
    } else if (permissions.equals(ObjectPermissions.ReadAndEditMetadata)) {
      ret = constants.permission_object_ReadAndEditMetadata();
    } else if (permissions.equals(ObjectPermissions.FullControl)) {
      ret = constants.permission_object_FullControl();
    } else {
      ret = "Custom(" + (permissions.isRead() ? "read," : "no read,")
        + (permissions.isEditMetadata() ? "edit," : "no edit,") + (permissions.isRemove() ? "remove," : "no remove,")
        + (permissions.isGrant() ? "grant)," : "no grant)");
    }
    return ret;
  }

  /**
   * Get permissions
   * 
   * @return the updated permissions
   */
  public ObjectPermissions getPermissions() {
    return permissions;
  }

}
