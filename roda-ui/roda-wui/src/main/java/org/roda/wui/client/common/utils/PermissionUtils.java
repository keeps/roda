package org.roda.wui.client.common.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public class PermissionUtils {
  public static final String HAS_NO_PERMISSION_CLASS = "hasNoPermissions";

  private PermissionUtils() {
    // do nothing
  }

  public static <T extends IsIndexed> boolean hasPermissions(List<String> methods, Permissions permissions) {
    boolean canAct = true;
    Optional<User> authenticatedUser = UserLogin.getInstance().getCachedUser();

    if (authenticatedUser.isPresent()) {
      User user = authenticatedUser.get();

      for (String method : methods) {
        canAct &= user.hasRole(ConfigurationManager.getString("core.roles." + method));

        String permissionKey = ConfigurationManager.getString("core.permissions." + method);
        if (canAct && permissionKey != null) {
          PermissionType permissionType = PermissionType.valueOf(permissionKey);

          if (permissions != null && permissionType != null) {
            if (permissions.getUserPermissions(user.getName()).contains(permissionType)) {
              canAct = true;
            } else {
              boolean containGroup = false;
              for (String group : user.getGroups()) {
                if (permissions.getGroupPermissions(group).contains(permissionType)) {
                  containGroup = true;
                  break;
                }
              }

              canAct = containGroup;
            }
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
