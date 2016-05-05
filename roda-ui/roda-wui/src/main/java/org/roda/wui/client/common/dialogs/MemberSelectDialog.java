package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.lists.RodaMemberList;

public class MemberSelectDialog extends DefaultSelectDialog<RODAMember, Void> {

  public MemberSelectDialog(String title, Filter filter) {
    super(title, filter, RodaConstants.MEMBERS_NAME, new RodaMemberList(filter, null, title, false));
  }

}