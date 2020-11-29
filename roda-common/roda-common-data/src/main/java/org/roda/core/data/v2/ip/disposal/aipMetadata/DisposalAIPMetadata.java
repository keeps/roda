package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalAIPMetadata implements Serializable {

  private static final long serialVersionUID = 1157420197892519914L;

  private DisposalScheduleAIPMetadata schedule;
  private List<DisposalHoldAIPMetadata> holds;
  private List<DisposalTransitiveHoldAIPMetadata> transitiveHolds;
  private DisposalConfirmationAIPMetadata confirmation;

  public DisposalAIPMetadata() {
    holds = new ArrayList<>();
    transitiveHolds = new ArrayList<>();
  }

  public DisposalScheduleAIPMetadata getSchedule() {
    return schedule;
  }

  public void setSchedule(DisposalScheduleAIPMetadata schedule) {
    this.schedule = schedule;
  }

  public List<DisposalHoldAIPMetadata> getHolds() {
    return holds;
  }

  public void setHolds(List<DisposalHoldAIPMetadata> holds) {
    this.holds = holds;
  }

  public List<DisposalTransitiveHoldAIPMetadata> getTransitiveHolds() {
    return transitiveHolds;
  }

  public void setTransitiveHolds(List<DisposalTransitiveHoldAIPMetadata> transitiveHolds) {
    this.transitiveHolds = transitiveHolds;
  }

  public DisposalConfirmationAIPMetadata getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(DisposalConfirmationAIPMetadata confirmation) {
    this.confirmation = confirmation;
  }

  @JsonIgnore
  public DisposalTransitiveHoldAIPMetadata findTransitiveAip(String aipId) {
    if (transitiveHolds != null) {
      for (DisposalTransitiveHoldAIPMetadata transitiveHoldAIPMetadata : transitiveHolds) {
        for (String fromAIP : transitiveHoldAIPMetadata.getFromAIPs()) {
          if (fromAIP.equals(aipId)) {
            return transitiveHoldAIPMetadata;
          }
        }
      }
    }
    return null;
  }

  @JsonIgnore
  public void addTransitiveHold(DisposalTransitiveHoldAIPMetadata transitiveHold) {
    this.transitiveHolds.add(transitiveHold);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalAIPMetadata that = (DisposalAIPMetadata) o;
    if (!Objects.equals(schedule, that.schedule))
      return false;
    if (!Objects.equals(holds, that.holds))
      return false;
    return Objects.equals(confirmation, that.confirmation);
  }

  @Override
  public int hashCode() {
    int result = schedule != null ? schedule.hashCode() : 0;
    result = 31 * result + (holds != null ? holds.hashCode() : 0);
    result = 31 * result + (confirmation != null ? confirmation.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalAIPMetadata{" + "schedule=" + schedule + ", holds=" + holds + ", confirmation=" + confirmation
      + '}';
  }

  @JsonIgnore
  public DisposalHoldAIPMetadata findHold(String DisposalHoldId) {
    if(holds != null) {
      for (DisposalHoldAIPMetadata hold : holds) {
        if(hold.getId().equals(DisposalHoldId)) {
          return hold;
        }
      }
    }
    return null;
  }

  @JsonIgnore
  public DisposalTransitiveHoldAIPMetadata findTransitiveHold(String transitiveHoldId) {
    if(transitiveHolds != null) {
      for (DisposalTransitiveHoldAIPMetadata hold : transitiveHolds) {
        if(hold.getId().equals(transitiveHoldId)) {
          return hold;
        }
      }
    }
    return null;
  }

  @JsonIgnore
  public void addDisposalHold(DisposalHoldAIPMetadata disposalHoldAIPMetadata) {
    if (holds == null) {
      holds = new ArrayList<>();
    }
    holds.add(disposalHoldAIPMetadata);
  }

  @JsonIgnore
  public boolean isAIPOnHold(String disposalHoldId) {
    if(holds != null) {
      for (DisposalHoldAIPMetadata hold : holds) {
        if (hold.getId().equals(disposalHoldId)) {
          return true;
        }
      }
    }
    return false;
  }

  @JsonIgnore
  public boolean onHold() {
    return !holds.isEmpty() || !transitiveHolds.isEmpty();
  }

  @JsonIgnore
  public String getDisposalScheduleId() {
    if(schedule != null) {
      return schedule.getId();
    }
    return null;
  }

  @JsonIgnore
  public String getDisposalConfirmationId() {
    if(confirmation != null) {
      return confirmation.getId();
    }
    return null;
  }

  @JsonIgnore
  public AIPDisposalScheduleAssociationType getDisposalScheduleAssociationType(){
    if(schedule != null){
      return schedule.getAssociationType();
    }
    return null;
  }

  @JsonIgnore
  public boolean removeDisposalHold(String disposalHold) {
    if(holds != null) {
      return holds.removeIf(hold -> hold.getId().equals(disposalHold));
    }
    return false;
  }

  @JsonIgnore
  public boolean removeTransitiveHold(String transitiveDisposalHold) {
    if(transitiveHolds != null) {
      return transitiveHolds.removeIf(transitiveHolds -> transitiveHolds.getId().equals(transitiveDisposalHold));
    }
    return false;
  }
}
