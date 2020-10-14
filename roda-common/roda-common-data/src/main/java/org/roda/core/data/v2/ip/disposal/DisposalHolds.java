package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.roda.core.data.v2.common.RODAObjectList;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLDS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHolds implements RODAObjectList<DisposalHold> {
  private List<DisposalHold> disposalHoldList;

  public DisposalHolds() {
    super();
    disposalHoldList = new ArrayList<>();
  }

  public DisposalHolds(List<DisposalHold> DisposalHolds) {
    super();
    disposalHoldList = DisposalHolds;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE)
  public List<DisposalHold> getObjects() {
    return disposalHoldList;
  }

  @Override
  public void setObjects(List<DisposalHold> disposalHolds) {
    this.disposalHoldList = disposalHolds;
  }

  @Override
  public void addObject(DisposalHold disposalHold) {
    this.disposalHoldList.add(disposalHold);
  }
}