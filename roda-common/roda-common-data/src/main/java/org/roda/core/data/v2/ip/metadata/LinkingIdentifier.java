/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.List;

public class LinkingIdentifier implements Serializable {
  private static final long serialVersionUID = -7150474519365697758L;

  private String type;
  private String value;
  private List<String> roles;

  public LinkingIdentifier() {
    super();
  }

  public LinkingIdentifier(String type, String value, List<String> roles) {
    super();
    this.type = type;
    this.value = value;
    this.roles = roles;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "LinkingIdentifier [type=" + type + ", value=" + value + ", roles=" + roles + "]";
  }

}
