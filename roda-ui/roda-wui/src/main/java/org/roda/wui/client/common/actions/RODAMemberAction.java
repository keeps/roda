package org.roda.wui.client.common.actions;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.RODAMember;

import java.util.Arrays;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public enum RODAMemberAction implements Actionable.Action<RODAMember> {
  NEW_USER(RodaConstants.PERMISSION_METHOD_CREATE_USER), NEW_GROUP(RodaConstants.PERMISSION_METHOD_CREATE_GROUP),
  ACTIVATE(RodaConstants.PERMISSION_METHOD_UPDATE_USER), DEACTIVATE(RodaConstants.PERMISSION_METHOD_UPDATE_USER),
  CHANGE_PASSWORD(RodaConstants.PERMISSION_METHOD_UPDATE_USER),
  EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_USER), REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_USER),
  ADD_NEW_GROUP(RodaConstants.PERMISSION_METHOD_UPDATE_USER),
  ADD_NEW_MEMBER(RodaConstants.PERMISSION_METHOD_UPDATE_USER),
  EDIT_PERMISSIONS(RodaConstants.PERMISSION_METHOD_UPDATE_USER),
  NEW_ACCESS_KEY(RodaConstants.PERMISSION_METHOD_CREATE_ACCESS_KEY);

  private final List<String> methods;

  RODAMemberAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
