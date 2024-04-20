/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.hold;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHolds implements RODAObjectList<DisposalHold> {
  @Serial
  private static final long serialVersionUID = -7273926545808506951L;
  private List<DisposalHold> disposalHoldList;

  public DisposalHolds() {
    super();
    disposalHoldList = new ArrayList<>();
  }

  public DisposalHolds(List<DisposalHold> disposalHolds) {
    super();
    disposalHoldList = disposalHolds;
  }

  @Override
  @JsonProperty(value = "disposalHolds")
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