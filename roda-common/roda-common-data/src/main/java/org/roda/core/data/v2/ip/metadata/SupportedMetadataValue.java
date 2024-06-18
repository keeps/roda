package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class SupportedMetadataValue implements Serializable {

  private Set<MetadataValue> value;

  public SupportedMetadataValue() {
    // do nothing
  }

  public SupportedMetadataValue(String template, Set<MetadataValue> value) {
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
