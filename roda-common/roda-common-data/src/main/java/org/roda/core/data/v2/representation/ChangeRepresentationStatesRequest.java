package org.roda.core.data.v2.representation;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class ChangeRepresentationStatesRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private SelectedItemsRequest items;
  private List<String> newStates;
  private String details;

  public ChangeRepresentationStatesRequest() {
    this.newStates = new ArrayList<>();
  }

  public ChangeRepresentationStatesRequest(SelectedItemsRequest items, List<String> newStates, String details) {
    this.items = items;
    this.newStates = newStates;
    this.details = details;
  }

  public SelectedItemsRequest getItems() {
    return items;
  }

  public void setItems(SelectedItemsRequest items) {
    this.items = items;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public List<String> getNewStates() {
    return newStates;
  }

  public void setNewStates(List<String> newStates) {
    this.newStates = newStates;
  }
}
