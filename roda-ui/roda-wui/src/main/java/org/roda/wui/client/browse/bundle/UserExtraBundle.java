/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.roda.wui.client.browse.MetadataValue;

public class UserExtraBundle implements Serializable {

  private static final long serialVersionUID = 7732750615662574335L;

  private String userName;
  private Set<MetadataValue> values;

  public UserExtraBundle() {
    super();
  }

  public UserExtraBundle(String userName) {
    super();
    this.userName = userName;
    this.values = new HashSet<>();
  }

  public UserExtraBundle(String userName, Set<MetadataValue> values) {
    super();
    this.userName = userName;
    this.values = values;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
