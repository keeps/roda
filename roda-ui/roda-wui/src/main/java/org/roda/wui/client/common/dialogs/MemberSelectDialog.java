/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.lists.RodaMemberList;

public class MemberSelectDialog extends DefaultSelectDialog<RODAMember, Void> {

  public MemberSelectDialog(String title, Filter filter) {
    super(title, filter, RodaConstants.MEMBERS_NAME,
      new RodaMemberList("MemberSelectDialog_rodaMembers", filter, title, false),
      false);
  }

}
