package org.roda.core.data.v2.representation;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RepresentationTypeOptions implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private boolean isControlledVocabulary = false;
  private List<String> types;

  public RepresentationTypeOptions(boolean isControlledVocabulary, List<String> types) {
    this.isControlledVocabulary = isControlledVocabulary;
    this.types = types;
  }

  public RepresentationTypeOptions() {
    this.types = new ArrayList<>();
  }

  public boolean isControlledVocabulary() {
    return isControlledVocabulary;
  }

  public void setControlledVocabulary(boolean controlledVocabulary) {
    isControlledVocabulary = controlledVocabulary;
  }

  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }
}
