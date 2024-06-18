package org.roda.core.data.v2.ip.metadata;

import java.util.List;

import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class DescriptiveMetadataVersionsResponse {

  private DescriptiveMetadataInfo descriptiveMetadata;
  private List<ResourceVersion> versions;
  private Permissions permissions;

  public DescriptiveMetadataVersionsResponse() {
    super();
  }

  public DescriptiveMetadataVersionsResponse(DescriptiveMetadataInfo descriptiveMetadata,
                                             List<ResourceVersion> versions, Permissions permissions) {
    super();
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
