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
public class DisposalConfirmations implements RODAObjectList<DisposalConfirmationMetadata> {
  private static final long serialVersionUID = -690695513750502079L;
  private List<DisposalConfirmationMetadata> disposalConfirmationMetadataList;

  public DisposalConfirmations() {
    super();
    disposalConfirmationMetadataList = new ArrayList<>();
  }

  public DisposalConfirmations(List<DisposalConfirmationMetadata> disposalConfirmationMetadataList) {
    super();
    this.disposalConfirmationMetadataList = disposalConfirmationMetadataList;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATIONS_METADATA)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATION_METADATA)
  public List<DisposalConfirmationMetadata> getObjects() {
    return disposalConfirmationMetadataList;
  }

  @Override
  public void setObjects(List<DisposalConfirmationMetadata> disposalConfirmationMetadataList) {
    this.disposalConfirmationMetadataList = disposalConfirmationMetadataList;
  }

  @Override
  public void addObject(DisposalConfirmationMetadata confirmation) {
    this.disposalConfirmationMetadataList.add(confirmation);
  }
}
