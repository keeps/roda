package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLDS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHolds {
  private List<DisposalHold> disposalHoldList;

  public DisposalHolds() {
    super();
    disposalHoldList = new ArrayList<>();
  }

  public DisposalHolds(List<DisposalHold> DisposalHolds) {
    super();
    disposalHoldList = DisposalHolds;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_HOLDS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD)
  public List<DisposalHold> getDisposalHolds() {
    return disposalHoldList;
  }

  public void setDisposalHolds(List<DisposalHold> disposalHolds) {
    this.disposalHoldList = disposalHolds;
  }

  public void addDisposalHold(DisposalHold disposalHold) {
    this.disposalHoldList.add(disposalHold);
  }
}