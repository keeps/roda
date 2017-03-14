/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPORTS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reports implements RODAObjectList<Report> {
  private static final long serialVersionUID = 1762663995829545566L;
  private List<Report> reportList;

  public Reports() {
    super();
    reportList = new ArrayList<>();
  }

  public Reports(List<Report> reports) {
    super();
    this.reportList = reports;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_REPORTS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_REPORT)
  public List<Report> getObjects() {
    return reportList;
  }

  @Override
  public void setObjects(List<Report> reports) {
    this.reportList = reports;
  }

  @Override
  public void addObject(Report report) {
    this.reportList.add(report);
  }

}
