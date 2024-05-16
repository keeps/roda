package org.roda.core.data.v2.generics;

import org.roda.core.data.v2.common.Pair;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RepresentationTypeOptions implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private Pair<Boolean, List<String>> representationTypeOptions;

  public RepresentationTypeOptions() {
    this.representationTypeOptions = new Pair<>();
  }

  public RepresentationTypeOptions(Pair<Boolean, List<String>> representationTypeOptions) {
    this.representationTypeOptions = representationTypeOptions;
  }

  public Pair<Boolean, List<String>> getRepresentationTypeOptions() {
    return representationTypeOptions;
  }

  public void setRepresentationTypeOptions(Pair<Boolean, List<String>> representationTypeOptions) {
    this.representationTypeOptions = representationTypeOptions;
  }
}
