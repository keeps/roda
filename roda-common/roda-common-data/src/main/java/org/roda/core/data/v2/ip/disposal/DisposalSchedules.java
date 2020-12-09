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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalSchedules implements RODAObjectList<DisposalSchedule> {
  private static final long serialVersionUID = 6731736012367265785L;
  private List<DisposalSchedule> disposalScheduleList;

  public DisposalSchedules() {
    super();
    disposalScheduleList = new ArrayList<>();
  }

  public DisposalSchedules(List<DisposalSchedule> disposalSchedules) {
    super();
    disposalScheduleList = disposalSchedules;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE)
  public List<DisposalSchedule> getObjects() {
    return disposalScheduleList;
  }

  @Override
  public void setObjects(List<DisposalSchedule> disposalSchedules) {
    this.disposalScheduleList = disposalSchedules;
  }

  @Override
  public void addObject(DisposalSchedule disposalSchedule) {
    this.disposalScheduleList.add(disposalSchedule);
  }
}
