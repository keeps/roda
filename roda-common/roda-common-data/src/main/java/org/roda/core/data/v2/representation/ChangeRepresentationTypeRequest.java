package org.roda.core.data.v2.representation;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ChangeRepresentationTypeRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private SelectedItemsRequest items;
  private String type;
  private String details;

  public ChangeRepresentationTypeRequest() {
    // empty constructor
  }

  public ChangeRepresentationTypeRequest(SelectedItemsRequest items, String type, String details) {
    this.items = items;
    this.type = type;
    this.details = details;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public SelectedItemsRequest getItems() {
    return items;
  }

  public void setItems(SelectedItemsRequest items) {
    this.items = items;
  }
}
