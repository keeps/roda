/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalRules implements RODAObjectList<DisposalRule> {

  private List<DisposalRule> disposalRuleList;

  public DisposalRules() {
    super();
    disposalRuleList = new ArrayList<>();
  }

  public DisposalRules(List<DisposalRule> disposalRules) {
    super();
    disposalRuleList = disposalRules;
  }

  @Override
  @JsonProperty(value = "disposalRules")
  public List<DisposalRule> getObjects() {
    return disposalRuleList;
  }

  @Override
  public void setObjects(List<DisposalRule> disposalRules) {
    this.disposalRuleList = disposalRules;
  }

  @Override
  public void addObject(DisposalRule disposalRule) {
    this.disposalRuleList.add(disposalRule);
  }

  public void sortRules() {
    Collections.sort(disposalRuleList);
  }

  public void incrementOrder(int index) {
    for (int i = index; i < disposalRuleList.size(); i++) {
      disposalRuleList.get(i).setOrder(i);
    }
  }

  public void decrementOrder(int startInterval, int endLimit) {
    for (int i = startInterval; i < endLimit; i++) {
      disposalRuleList.get(i).setOrder(i);
    }
  }

  public void moveToTop(DisposalRule disposalRule) {
    disposalRule.setOrder(0);
    disposalRuleList.remove(disposalRule);
    disposalRuleList.add(0, disposalRule);
    incrementOrder(1);
  }

  public void moveToBottom(DisposalRule disposalRule, int startInterval) {
    int lastIndex = disposalRuleList.size() - 1;
    disposalRule.setOrder(lastIndex);
    disposalRuleList.remove(disposalRule);
    disposalRuleList.add(disposalRule);
    decrementOrder(startInterval, lastIndex);
  }
}
