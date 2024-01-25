/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class UserProfile implements Serializable {
  private static final long serialVersionUID = -117396300862413045L;
  private String title;
  private String profile;
  private String description;
  private boolean dissemination;
  private boolean representation;
  private Map<String, String> options;

  public UserProfile() {
    options = new HashMap<>();
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public void setHasRepresentation(boolean representation) {
    this.representation = representation;
  }

  public void setHasDissemination(boolean dissemination) {
    this.dissemination = dissemination;
  }

  public boolean isDissemination() {
    return dissemination;
  }

  public boolean isRepresentation() {
    return representation;
  }

  public String getTitle() {
    return title;
  }

  public String getProfile() {
    return profile;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getOptions() {
    return options;
  }
}
