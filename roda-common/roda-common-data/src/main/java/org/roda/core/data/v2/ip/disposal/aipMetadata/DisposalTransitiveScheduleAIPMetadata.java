package org.roda.core.data.v2.ip.disposal.aipMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_TRANSITIVE_SCHEDULE_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalTransitiveScheduleAIPMetadata implements Serializable {
  private static final long serialVersionUID = -3946608571760723840L;

  private String aipId;
  private Date overDueDate;
  private DisposalActionCode actionCode;
  private Integer retentionPeriodDuration;

  public DisposalTransitiveScheduleAIPMetadata() {
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public Date getOverDueDate() {
    return overDueDate;
  }

  public void setOverDueDate(Date overDueDate) {
    this.overDueDate = overDueDate;
  }

  public DisposalActionCode getActionCode() {
    return actionCode;
  }

  public void setActionCode(DisposalActionCode actionCode) {
    this.actionCode = actionCode;
  }

  public Integer getRetentionPeriodDuration() {
    return retentionPeriodDuration;
  }

  public void setRetentionPeriodDuration(Integer retentionPeriodDuration) {
    this.retentionPeriodDuration = retentionPeriodDuration;
  }
}
