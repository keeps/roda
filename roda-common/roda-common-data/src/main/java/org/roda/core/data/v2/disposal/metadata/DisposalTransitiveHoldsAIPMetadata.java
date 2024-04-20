/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.metadata;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalTransitiveHoldsAIPMetadata implements RODAObjectList<DisposalTransitiveHoldAIPMetadata> {
  @Serial
  private static final long serialVersionUID = 2571630535422685722L;
  private List<DisposalTransitiveHoldAIPMetadata> disposalTransitiveHoldAIPMetadataList;

  public DisposalTransitiveHoldsAIPMetadata() {
    super();
    disposalTransitiveHoldAIPMetadataList = new ArrayList<>();
  }

  @Override
  @JsonProperty(value = "disposalTransitiveHolds")
  public List<DisposalTransitiveHoldAIPMetadata> getObjects() {
    return disposalTransitiveHoldAIPMetadataList;
  }

  @Override
  public void setObjects(List<DisposalTransitiveHoldAIPMetadata> disposalTransitiveHoldsAIPMetadata) {
    this.disposalTransitiveHoldAIPMetadataList = disposalTransitiveHoldsAIPMetadata;
  }

  @Override
  public void addObject(DisposalTransitiveHoldAIPMetadata disposalTransitiveHoldAIPMetadata) {
    this.disposalTransitiveHoldAIPMetadataList.add(disposalTransitiveHoldAIPMetadata);
  }

  @JsonIgnore
  public boolean isEmpty() {
    return disposalTransitiveHoldAIPMetadataList.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalTransitiveHoldsAIPMetadata that = (DisposalTransitiveHoldsAIPMetadata) o;

    return Objects.equals(disposalTransitiveHoldAIPMetadataList, that.disposalTransitiveHoldAIPMetadataList);
  }

  @Override
  public int hashCode() {
    return disposalTransitiveHoldAIPMetadataList != null ? disposalTransitiveHoldAIPMetadataList.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "DisposalTransitiveHoldsAIPMetadata{" + "disposalTransitiveHoldsAIPMetadataList=" + disposalTransitiveHoldAIPMetadataList + '}';
  }
}
