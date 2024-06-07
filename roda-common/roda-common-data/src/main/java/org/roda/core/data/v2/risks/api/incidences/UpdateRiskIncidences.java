/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks.api.incidences;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRiskIncidences implements Serializable {

  @Serial
  private static final long serialVersionUID = -878964145498801854L;

  private SelectedItemsRequest selectedItems;
  private String mitigatedDescription;
  private String severity;
  private String status;

  public UpdateRiskIncidences() {
    // empty constructor
  }

  public UpdateRiskIncidences(SelectedItems<?> selectedItems, String mitigatedDescription, String severity,
    String status) {
    this.selectedItems = SelectedItemsUtils.convertToRESTRequest(selectedItems);
    this.mitigatedDescription = mitigatedDescription;
    this.severity = severity;
    this.status = status;
  }

  public SelectedItemsRequest getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItemsRequest selectedItems) {
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
