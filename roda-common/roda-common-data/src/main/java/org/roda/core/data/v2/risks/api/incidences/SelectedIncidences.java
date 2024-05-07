/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks.api.incidences;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.roda.core.data.v2.risks.RiskIncidence;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("SelectedIncidences")
public class SelectedIncidences<T extends IsRODAObject> {

  private SelectedItems<T> selectedItems;
  private String mitigatedDescription;
  private String severity;
  private String status;

  // Constructor
  public SelectedIncidences(){
    // do nothing
  }


  public SelectedIncidences(SelectedItems<T> selectedItems, String mitigatedDescription, String severity, String status) {
    this.selectedItems = selectedItems;
    this.mitigatedDescription = mitigatedDescription;
    this.severity = severity;
    this.status = status;
  }

  public SelectedItems<T> getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItems<T> selectedItems) {
    this.selectedItems = selectedItems;
  }

  public String getMitigatedDescription() {
    return mitigatedDescription;
  }

  public void setMitigatedDescription(String mitigatedDescription) {
    this.mitigatedDescription = mitigatedDescription;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
