package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class DescriptiveMetadataVersions implements Serializable {

  @Serial
  private static final long serialVersionUID = 2936574469592046228L;

  private DescriptiveMetadataInfo descriptiveMetadata;
  private List<ResourceVersion> versions;
  private Permissions permissions;

  public DescriptiveMetadataVersions() {
    versions = new ArrayList<>();
  }

  public DescriptiveMetadataVersions(DescriptiveMetadataInfo descriptiveMetadata,
                                     List<ResourceVersion> versions, Permissions permissions) {
    this.descriptiveMetadata = descriptiveMetadata;
    this.versions = versions;
    this.permissions = permissions;
  }

  public DescriptiveMetadataInfo getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(DescriptiveMetadataInfo descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public List<ResourceVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<ResourceVersion> versions) {
    this.versions = versions;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }
}
