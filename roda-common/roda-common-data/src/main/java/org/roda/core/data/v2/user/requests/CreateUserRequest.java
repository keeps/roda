package org.roda.core.data.v2.user.requests;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.common.SecureString;
import org.roda.core.data.v2.generics.MetadataValue;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateUserRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 2288990827132952813L;

  private String email;
  private String name;
  private String fullName;
  private Set<String> roles;
  private Set<String> groups;
  private boolean guest;
  private SecureString password;
  private Set<MetadataValue> values;

  public CreateUserRequest(String email, String name, String fullName, Set<String> roles, Set<String> groups,
    boolean guest, SecureString password, Set<MetadataValue> values) {
    this.email = email;
    this.name = name;
    this.fullName = fullName;
    this.roles = roles;
    this.groups = groups;
    this.guest = guest;
    this.password = password;
    this.values = values;
  }

  public CreateUserRequest() {
    this.groups = new HashSet<>();
    this.password = new SecureString();
    this.values = new HashSet<>();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public boolean isGuest() {
    return guest;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public boolean getGuest() {
    return guest;
  }

  public void setGuest(boolean guest) {
    this.guest = guest;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  public SecureString getPassword() {
    return password;
  }

  public void setPassword(SecureString password) {
    this.password = password;
  }
}
