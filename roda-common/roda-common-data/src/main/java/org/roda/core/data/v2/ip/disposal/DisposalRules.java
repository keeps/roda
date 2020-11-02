package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

  public void sortRules(){
    disposalRuleList = disposalRuleList.stream()
            .sorted(Comparator.comparing(DisposalRule::getOrder))
            .collect(Collectors.toList());
  }
}
