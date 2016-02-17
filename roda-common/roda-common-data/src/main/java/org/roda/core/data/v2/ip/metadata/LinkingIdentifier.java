package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.List;

public class LinkingIdentifier implements Serializable {
  private static final long serialVersionUID = -7150474519365697758L;
  String type;
  String value;
  List<String> roles;
  
  public LinkingIdentifier() {
    super();
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
