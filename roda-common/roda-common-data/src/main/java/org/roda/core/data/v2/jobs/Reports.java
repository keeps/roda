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

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "reports")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reports implements RODAObjectList<Report> {
  private List<Report> reports;

  public Reports() {
    super();
    reports = new ArrayList<Report>();
  }

  public Reports(List<Report> reports) {
    super();
    this.reports = reports;
  }

  @JsonProperty(value = "reports")
  @XmlElement(name = "report")
  public List<Report> getObjects() {
    return reports;
  }

  @Override
  public void setObjects(List<Report> reports) {
    this.reports = reports;
  }

  @Override
  public void addObject(Report report) {
    this.reports.add(report);
  }

}
