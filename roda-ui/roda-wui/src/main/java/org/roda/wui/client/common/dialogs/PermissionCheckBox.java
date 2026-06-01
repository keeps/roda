/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.ip.Permissions.PermissionType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;

import config.i18n.client.ClientMessages;

/**
 * A CheckBox that holds a {@link PermissionType} value and displays the
 * corresponding i18n label and description.
 */
public class PermissionCheckBox extends CheckBox {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final PermissionType permissionType;

  public PermissionCheckBox(PermissionType permissionType) {
    super(messages.objectPermission(permissionType));
    setTitle(messages.objectPermissionDescription(permissionType));
    this.permissionType = permissionType;
  }

  public PermissionType getPermissionType() {
    return permissionType;
  }
}

