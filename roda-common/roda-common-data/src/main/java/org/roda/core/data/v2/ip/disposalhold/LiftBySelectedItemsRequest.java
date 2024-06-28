package org.roda.core.data.v2.ip.disposalhold;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LiftBySelectedItemsRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 7811248075823072309L;

  private SelectedItemsRequest selectedItems;
  private String details;

  public LiftBySelectedItemsRequest() {
    // empty constructor
  }

  public SelectedItemsRequest getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItemsRequest selectedItems) {
    this.selectedItems = selectedItems;
  }

  @JsonIgnore
  public void setSelectedItems(SelectedItems<?> selectedItems) {
    this.selectedItems = SelectedItemsUtils.convertToRESTRequest(selectedItems);
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
