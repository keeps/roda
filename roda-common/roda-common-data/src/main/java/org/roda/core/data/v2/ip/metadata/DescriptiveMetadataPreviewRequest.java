package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DescriptiveMetadataPreviewRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -5444437924544332926L;

  private String id;
  private Set<MetadataValue> value;

  public DescriptiveMetadataPreviewRequest() {
    // do nothing
  }

  public DescriptiveMetadataPreviewRequest(String id, Set<MetadataValue> value) {
    this.id = id;
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<MetadataValue> getValue() {
    return value;
  }

  public void setValue(Set<MetadataValue> value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "SupportedMetadataValue{" + "id='" + id + '\'' + ", value=" + value + '}';
  }
}
