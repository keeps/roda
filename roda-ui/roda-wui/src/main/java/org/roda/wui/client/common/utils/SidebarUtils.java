package org.roda.wui.client.common.utils;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SidebarUtils {
  public static void hideSidebar(Widget content, Widget sidebar) {
    toggleSidebar(content, sidebar, false);
  }

  public static void showSidebar(Widget content, Widget sidebar) {
    toggleSidebar(content, sidebar, true);
  }

  public static void toggleSidebar(Widget content, Widget sidebar, boolean show) {
    if (show) {
      content.addStyleName("col_10");
      content.removeStyleName("col_12");
      sidebar.setVisible(true);
    } else {
      content.addStyleName("col_12");
      content.removeStyleName("col_10");
      sidebar.setVisible(false);
    }
  }
}
