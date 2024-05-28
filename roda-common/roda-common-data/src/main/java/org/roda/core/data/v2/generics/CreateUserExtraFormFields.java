package org.roda.core.data.v2.generics;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class CreateUserExtraFormFields implements Serializable {
  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  Set<MetadataValue> extraFormFields;

  public CreateUserExtraFormFields() {
    this.extraFormFields = new HashSet<>();
  }

  public Set<MetadataValue> getExtraFormFields() {
    return extraFormFields;
  }

  public void setExtraFormFields(Set<MetadataValue> extraFormFields) {
    this.extraFormFields = extraFormFields;
  }

  public CreateUserExtraFormFields(Set<MetadataValue> extraFormFields) {
    this.extraFormFields = extraFormFields;
  }
}
