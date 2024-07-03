package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

public class SelectedType {

  @Serial
  private static final long serialVersionUID = 8626922937067856615L;

  private String metadataId;

  private Set<MetadataValue> value;

  public SelectedType() {
    // do nothing
  }

  public SelectedType(String metadataId, Set<MetadataValue> value) {
    this.metadataId = metadataId;
    this.value = value;
  }

  public String getMetadataId() {
    return metadataId;
  }

  public void setMetadataId(String metadataId) {
    this.metadataId = metadataId;
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
