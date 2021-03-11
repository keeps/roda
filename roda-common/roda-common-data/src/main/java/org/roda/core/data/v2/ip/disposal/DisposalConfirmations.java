/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATIONS_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalConfirmations implements RODAObjectList<DisposalConfirmation> {
  private static final long serialVersionUID = -690695513750502079L;
  private List<DisposalConfirmation> disposalConfirmationList;

  public DisposalConfirmations() {
    super();
    disposalConfirmationList = new ArrayList<>();
  }

  public DisposalConfirmations(List<DisposalConfirmation> disposalConfirmationList) {
    super();
    this.disposalConfirmationList = disposalConfirmationList;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATIONS_METADATA)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATION_METADATA)
  public List<DisposalConfirmation> getObjects() {
    return disposalConfirmationList;
  }

  @Override
  public void setObjects(List<DisposalConfirmation> disposalConfirmationList) {
    this.disposalConfirmationList = disposalConfirmationList;
  }

  @Override
  public void addObject(DisposalConfirmation confirmation) {
    this.disposalConfirmationList.add(confirmation);
  }
}
