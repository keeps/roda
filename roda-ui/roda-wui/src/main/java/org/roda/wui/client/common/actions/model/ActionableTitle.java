package org.roda.wui.client.common.actions.model;

import org.roda.wui.client.common.utils.StringUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableTitle {
  private final String title;

  ActionableTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public boolean hasTitle() {
    return StringUtils.isNotBlank(title);
  }
}
