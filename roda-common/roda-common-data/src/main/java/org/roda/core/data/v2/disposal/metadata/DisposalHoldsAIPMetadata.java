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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHoldsAIPMetadata implements RODAObjectList<DisposalHoldAIPMetadata> {
  @Serial
  private static final long serialVersionUID = 2571630535422685722L;
  private List<DisposalHoldAIPMetadata> disposalHoldAIPMetadataList;

  public DisposalHoldsAIPMetadata() {
    super();
    disposalHoldAIPMetadataList = new ArrayList<>();
  }

  @Override
  @JsonProperty(value = "disposalHoldsAIPMetadata")
  public List<DisposalHoldAIPMetadata> getObjects() {
    return disposalHoldAIPMetadataList;
  }

  @Override
  public void setObjects(List<DisposalHoldAIPMetadata> disposalHoldsAIPMetadata) {
    this.disposalHoldAIPMetadataList = disposalHoldsAIPMetadata;
  }

  @Override
  public void addObject(DisposalHoldAIPMetadata disposalHoldAIPMetadata) {
    this.disposalHoldAIPMetadataList.add(disposalHoldAIPMetadata);
  }

  @JsonIgnore
  public DisposalHoldAIPMetadata findDisposalHold(final String disposalHoldId) {
    for (DisposalHoldAIPMetadata hold : disposalHoldAIPMetadataList) {
      if (hold.getId().equals(disposalHoldId)) {
        return hold;
      }
    }

    return null;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return disposalHoldAIPMetadataList.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalHoldsAIPMetadata that = (DisposalHoldsAIPMetadata) o;

    return Objects.equals(disposalHoldAIPMetadataList, that.disposalHoldAIPMetadataList);
  }

  @Override
  public int hashCode() {
    return disposalHoldAIPMetadataList != null ? disposalHoldAIPMetadataList.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "DisposalHoldsAIPMetadata{" + "disposalHoldAIPMetadataList=" + disposalHoldAIPMetadataList + '}';
  }
}
