package org.roda.core.data.v2.generics;

import org.roda.core.data.v2.ip.IndexedRepresentation;

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

  private IndexedRepresentation indexedRepresentation;
  private List<String> newStates;

  public ChangeRepresentationStatesRequest() {
    this.indexedRepresentation = new IndexedRepresentation();
    this.newStates = new ArrayList<>();
  }

  public ChangeRepresentationStatesRequest(IndexedRepresentation indexedRepresentation, List<String> newStates) {
    this.indexedRepresentation = indexedRepresentation;
    this.newStates = newStates;
  }

  public IndexedRepresentation getIndexedRepresentation() {
    return indexedRepresentation;
  }

  public void setIndexedRepresentation(IndexedRepresentation indexedRepresentation) {
    this.indexedRepresentation = indexedRepresentation;
  }

  public List<String> getNewStates() {
    return newStates;
  }

  public void setNewStates(List<String> newStates) {
    this.newStates = newStates;
  }
}
