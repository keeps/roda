package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.io.Serializable;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_TRANSITIVE_HOLD_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalTransitiveHoldAIPMetadata implements Serializable {
  private static final long serialVersionUID = -1277210243520579523L;

  private String aipId;

  public DisposalTransitiveHoldAIPMetadata() {
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalTransitiveHoldAIPMetadata that = (DisposalTransitiveHoldAIPMetadata) o;

    return Objects.equals(aipId, that.aipId);
  }

  @Override
  public int hashCode() {
    return aipId != null ? aipId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "DisposalTransitiveHoldAIPMetadata{" + "aipId='" + aipId + '\'' + '}';
  }
}
