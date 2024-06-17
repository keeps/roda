package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class SupportedMetadataValue implements Serializable {

  private String template;
  private Set<MetadataValue> value;

  public SupportedMetadataValue() {
    // do nothing
  }

  public SupportedMetadataValue(String template, Set<MetadataValue> value) {
    this.template = template;
    this.value = value;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Set<MetadataValue> getValue() {
    return value;
  }

  public void setValue(Set<MetadataValue> value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "SupportedMetadataValue{" + "template='" + template + '\'' + ", value=" + value + '}';
  }
}
