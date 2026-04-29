/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.user.RODAMember;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NoActionsToolbar extends BrowseObjectActionsToolbar<RODAMember> {

  public void build() {
    setObjectAndBuild(null, null, null);
  }

  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
  }
}
