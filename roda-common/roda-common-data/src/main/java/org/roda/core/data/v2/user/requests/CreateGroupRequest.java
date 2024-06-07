package org.roda.core.data.v2.user.requests;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateGroupRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String name;
  private String fullName;
  private Set<String> directRoles;

  public CreateGroupRequest(String name, String fullName, Set<String> directRoles) {
    this.name = name;
    this.fullName = fullName;
    this.directRoles = directRoles;
  }

  public CreateGroupRequest() {
    this.name = null;
    this.fullName = null;
    this.directRoles = new HashSet<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<String> getDirectRoles() {
    return directRoles;
  }

  public void setDirectRoles(Set<String> directRoles) {
    this.directRoles = directRoles;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
}
