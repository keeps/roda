/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.Map;

public class ModelInfo implements Serializable {
  private static final long serialVersionUID = 100887226360606485L;

  private Map<String, Integer> installedClassesVersions;

  public ModelInfo() {
    super();
  }

  public Map<String, Integer> getInstalledClassesVersions() {
    return installedClassesVersions;
  }

  public ModelInfo setInstalledClassesVersions(Map<String, Integer> installedClassesVersions) {
    this.installedClassesVersions = installedClassesVersions;
    return this;
  }

}
