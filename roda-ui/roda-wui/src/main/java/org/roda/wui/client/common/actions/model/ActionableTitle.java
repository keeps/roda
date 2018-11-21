/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.model;

import org.roda.wui.common.client.tools.StringUtils;

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
