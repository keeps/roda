/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user.requests;

import org.roda.core.data.v2.generics.MetadataValue;

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

  private Set<MetadataValue> extraFormFields;

  public CreateUserExtraFormFields() {
    this.extraFormFields = new HashSet<>();
  }

  public CreateUserExtraFormFields(Set<MetadataValue> extraFormFields) {
    this.extraFormFields = extraFormFields;
  }

  public Set<MetadataValue> getExtraFormFields() {
    return extraFormFields;
  }

  public void setExtraFormFields(Set<MetadataValue> extraFormFields) {
    this.extraFormFields = extraFormFields;
  }
}
