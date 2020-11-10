package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLDS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHolds implements RODAObjectList<DisposalHold> {
  private static final long serialVersionUID = -7273926545808506951L;
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
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_HOLDS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD)
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

  @JsonIgnore
  public DisposalHold findDisposalHold(final String disposalHoldId) {
    for (DisposalHold hold : disposalHoldList) {
      if (hold.getId().equals(disposalHoldId)) {
        return hold;
      }
    }

    return null;
  }
}