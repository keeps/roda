/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class ConversionProfile implements Serializable {
  @Serial
  private static final long serialVersionUID = -117396300862413045L;
  private String title;
  private String profile;
  private String description;
  private boolean canBeUsedForDissemination;
  private boolean canBeUsedForRepresentation;
  private Map<String, String> options;

  public ConversionProfile() {
    options = new HashMap<>();
  }

  public void setCanBeUsedForRepresentation(boolean representation) {
    this.canBeUsedForRepresentation = representation;
  }

  public void setCanBeUsedForDissemination(boolean dissemination) {
    this.canBeUsedForDissemination = dissemination;
  }

  public boolean canBeUsedForDissemination() {
    return canBeUsedForDissemination;
  }

  public boolean canBeUsedForRepresentation() {
    return canBeUsedForRepresentation;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
