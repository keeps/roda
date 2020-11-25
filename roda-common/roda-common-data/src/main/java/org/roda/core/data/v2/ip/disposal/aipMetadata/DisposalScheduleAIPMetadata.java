package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalScheduleAIPMetadata implements IsModelObject {
  private static final long serialVersionUID = -2115265583456966379L;

  private String id;
  private String associatedBy;
  private Date associatedOn;
  private AIPDisposalScheduleAssociationType associationType;
  private List<DisposalTransitiveScheduleAIPMetadata> transitive;

  public DisposalScheduleAIPMetadata() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssociatedBy() {
    return associatedBy;
  }

  public void setAssociatedBy(String associatedBy) {
    this.associatedBy = associatedBy;
  }

  public Date getAssociatedOn() {
    return associatedOn;
  }

  public void setAssociatedOn(Date associatedOn) {
    this.associatedOn = associatedOn;
  }

  public AIPDisposalScheduleAssociationType getAssociationType() {
    return associationType;
  }

  public void setAssociationType(AIPDisposalScheduleAssociationType associationType) {
    this.associationType = associationType;
  }

  public List<DisposalTransitiveScheduleAIPMetadata> getTransitive() {
    return transitive;
  }

  public void setTransitive(List<DisposalTransitiveScheduleAIPMetadata> transitive) {
    this.transitive = transitive;
  }

  @JsonIgnore
  public void addTransitiveAip(DisposalTransitiveScheduleAIPMetadata transitiveAip) {
    if (transitive == null) {
      transitive = new ArrayList<>();
    }
    this.transitive.add(transitiveAip);
  }

  @JsonIgnore
  public DisposalTransitiveScheduleAIPMetadata findTransitiveAip(String aipId) {
    if(transitive != null) {
      for (DisposalTransitiveScheduleAIPMetadata transitiveScheduleAIPMetadata : transitive) {
        if (transitiveScheduleAIPMetadata.getAipId().equals(aipId)){
          return transitiveScheduleAIPMetadata;
        }
      }
    }
    return null;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 0;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalScheduleAIPMetadata that = (DisposalScheduleAIPMetadata) o;

    if (!Objects.equals(id, that.id))
      return false;
    if (!Objects.equals(associatedBy, that.associatedBy))
      return false;
    if (!Objects.equals(associatedOn, that.associatedOn))
      return false;
    return associationType == that.associationType;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (associatedBy != null ? associatedBy.hashCode() : 0);
    result = 31 * result + (associatedOn != null ? associatedOn.hashCode() : 0);
    result = 31 * result + (associationType != null ? associationType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalScheduleAIPMetadata{" + "id='" + id + '\'' + ", associatedBy='" + associatedBy + '\''
      + ", associatedOn='" + associatedOn + '\'' + ", flow=" + associationType + '}';
  }
}
