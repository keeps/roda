/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_TRANSITIVE_HOLD_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalTransitiveHoldAIPMetadata implements Serializable {
  private static final long serialVersionUID = -1277210243520579523L;

  private String id;
  private List<String> fromAIPs;

  public DisposalTransitiveHoldAIPMetadata() {
    fromAIPs = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getFromAIPs() {
    return fromAIPs;
  }

  public void setFromAIPs(List<String> fromAIPs) {
    this.fromAIPs = fromAIPs;
  }

  @JsonIgnore
  public void addFromAip(String aipId){
    this.fromAIPs.add(aipId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalTransitiveHoldAIPMetadata that = (DisposalTransitiveHoldAIPMetadata) o;

    if (!Objects.equals(id, that.id))
      return false;
    return Objects.equals(fromAIPs, that.fromAIPs);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (fromAIPs != null ? fromAIPs.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalTransitiveHoldAIPMetadata{" + "id='" + id + '\'' + ", fromAIPs=" + fromAIPs + '}';
  }

  public String findAIP(String aipId) {
    if(fromAIPs != null && !fromAIPs.isEmpty()) {
      for (String fromAIP : fromAIPs) {
       if(fromAIP.equals(aipId)){
         return aipId;
       }
      }
    }
    return null;
  }
}
