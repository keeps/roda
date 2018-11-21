/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public class PermissionClientUtils {
  public static final String HAS_NO_PERMISSION_CLASS = "hasNoPermissions";

  private PermissionClientUtils() {
    // do nothing
  }

  public static boolean hasPermissions(String... methods) {
    return hasPermissions(Arrays.asList(methods), null);
  }

  public static boolean hasPermissions(Permissions permissions, String... methods) {
    return hasPermissions(Arrays.asList(methods), permissions);
  }

  public static boolean hasPermissions(List<String> methods, Permissions permissions) {
    Optional<User> authenticatedUser = UserLogin.getInstance().getCachedUser();
    boolean canAct = true;

    if (authenticatedUser.isPresent()) {
      User user = authenticatedUser.get();

      for (String method : methods) {
        canAct &= user.hasRole(ConfigurationManager.getString("core.roles." + method));

        String permissionKey = ConfigurationManager.getString("core.permissions." + method);
        if (canAct && permissions != null && permissionKey != null) {
          try {
            PermissionType permissionType = PermissionType.valueOf(permissionKey);

            if (!permissions.getUserPermissions(user.getName()).contains(permissionType)) {
              boolean containGroup = false;
              for (String group : user.getGroups()) {
                if (permissions.getGroupPermissions(group).contains(permissionType)) {
                  containGroup = true;
                  break;
                }
              }

              canAct = containGroup;
            }
          } catch(IllegalArgumentException e) {
            // do nothing
          }
        }
      }
    }

    return canAct;
  }

  public static void bindPermission(Widget widget, Permissions permissions, String... methods) {
    boolean hasPermissions = hasPermissions(Arrays.asList(methods), permissions);

    if (widget instanceof Button) {
      ((Button) widget).setEnabled(hasPermissions);
    }

    if (hasPermissions) {
      widget.removeStyleName(HAS_NO_PERMISSION_CLASS);
    } else {
      widget.addStyleName(HAS_NO_PERMISSION_CLASS);
    }
  }
}
