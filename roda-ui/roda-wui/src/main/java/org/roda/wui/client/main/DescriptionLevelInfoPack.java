/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.descriptionLevels.DescriptionLevel;

public class DescriptionLevelInfoPack implements Serializable {

  private static final long serialVersionUID = 3476633742750492225L;

  public List<DescriptionLevel> descriptionLevels;
  
  public String ghostClass;
  
  public String defaultClass;

  public DescriptionLevelInfoPack() {
  }

  public List<DescriptionLevel> getDescriptionLevels() {
    return descriptionLevels;
  }

  public void setDescriptionLevels(List<DescriptionLevel> descriptionLevels) {
    this.descriptionLevels = descriptionLevels;
  }

  public String getGhostClass() {
    return ghostClass;
  }

  public void setGhostClass(String ghostClass) {
    this.ghostClass = ghostClass;
  }

  public String getDefaultClass() {
    return defaultClass;
  }

  public void setDefaultClass(String defaultClass) {
    this.defaultClass = defaultClass;
  }
  
  
}
