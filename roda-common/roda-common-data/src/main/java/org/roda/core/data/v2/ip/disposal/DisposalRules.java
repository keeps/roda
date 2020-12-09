package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_RULES)
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
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE)
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
    decrementOrder(startInterval, lastIndex--);
  }
}
