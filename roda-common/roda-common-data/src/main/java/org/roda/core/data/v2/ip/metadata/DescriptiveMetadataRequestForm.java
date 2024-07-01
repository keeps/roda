package org.roda.core.data.v2.ip.metadata;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

import java.io.Serial;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DescriptiveMetadataRequestForm")
public class DescriptiveMetadataRequestForm extends CreateDescriptiveMetadataRequest {

  @Serial
  private static final long serialVersionUID = -1436196026800433776L;

  private Set<MetadataValue> values;

  public DescriptiveMetadataRequestForm() {
    super();
  }

  public DescriptiveMetadataRequestForm(String id, String filename, String type, String version,
    boolean similar, Permissions permissions, Set<MetadataValue> values) {
    super(id, filename, type, version, similar, permissions);
    this.values = values;
  }


  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

}
