package org.roda.core.data.v2.representation;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class ChangeRepresentationStatesRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String representationId;
  private List<String> newStates;
  private String details;

  public ChangeRepresentationStatesRequest() {
    this.newStates = new ArrayList<>();
  }

  public ChangeRepresentationStatesRequest(String representationId, List<String> newStates, String details) {
    this.representationId = representationId;
    this.newStates = newStates;
    this.details = details;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
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
