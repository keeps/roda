package org.roda.core.data.v2.ip.metadata;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DescriptiveMetadataRequestForm")
public class DescriptiveMetadataRequestForm extends CreateDescriptiveMetadataRequest {

  private Set<MetadataValue> values;

  public DescriptiveMetadataRequestForm() {
    super();
  }

  public DescriptiveMetadataRequestForm(String id, String filename, String type, String version, String rawTemplate,
    boolean similar, Permissions permissions, Set<MetadataValue> values) {
    super(id, filename, type, version, rawTemplate, similar, permissions);
    this.values = values;
  }

  @Override
  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

}
