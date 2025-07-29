/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class SupportedMetadataValue implements Serializable {

  @Serial
  private static final long serialVersionUID = 4130516692186749472L;

  private Set<MetadataValue> value;

  public SupportedMetadataValue() {
    // do nothing
  }

  public SupportedMetadataValue(Set<MetadataValue> value) {
    this.value = value;
  }

  public Set<MetadataValue> getValue() {
    return value;
  }

  public void setValue(Set<MetadataValue> value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "SupportedMetadataValue{" + "value=" + value + '}';
  }
}
