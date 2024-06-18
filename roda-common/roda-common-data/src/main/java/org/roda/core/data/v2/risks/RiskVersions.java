package org.roda.core.data.v2.risks;

import org.roda.core.data.v2.ip.metadata.ResourceVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RiskVersions implements Serializable {
  @Serial
  private static final long serialVersionUID = -7350407856562068545L;

  private List<ResourceVersion> versions = new ArrayList<>();

  public RiskVersions() {
    // empty constructor
  }

  public List<ResourceVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<ResourceVersion> versions) {
    this.versions = versions;
  }

  public void addObject(ResourceVersion version) {
    this.versions.add(version);
  }
}
