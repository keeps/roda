package org.roda.wui.client.common.actions;

import org.roda.wui.client.common.utils.StringUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionsTitle {
  private final String title;

  ActionsTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public boolean hasTitle() {
    return StringUtils.isNotBlank(title);
  }
}
