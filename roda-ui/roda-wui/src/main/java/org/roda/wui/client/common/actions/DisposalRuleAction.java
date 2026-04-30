package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.rule.DisposalRule;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public enum DisposalRuleAction implements Actionable.Action<DisposalRule> {
  NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_RULE),
  EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_RULE),
  APPLY_RULES(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE),
  CHANGE_ORDER(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_RULE),
  REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_RULE);

  private final List<String> methods;

  DisposalRuleAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
