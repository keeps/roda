package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.index.select.SelectedItems;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationFilterRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 2752974091221486302L;

  private SelectedItems<RepresentationInformation> selectedItems;
  private String filterToAdd;

  public RepresentationInformationFilterRequest() {
    // empty constructor
  }

  public SelectedItems<RepresentationInformation> getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItems<RepresentationInformation> selectedItems) {
    this.selectedItems = selectedItems;
  }

  public String getFilterToAdd() {
    return filterToAdd;
  }

  public void setFilterToAdd(String filterToAdd) {
    this.filterToAdd = filterToAdd;
  }
}
