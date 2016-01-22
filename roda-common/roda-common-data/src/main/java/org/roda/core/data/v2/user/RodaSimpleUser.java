/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.io.Serializable;

public class RodaSimpleUser extends RodaPrincipal implements Serializable {
  private static final long serialVersionUID = 6514790636010895870L;

  private String email;
  private boolean guest;

  public RodaSimpleUser() {
    super();
  }

  public RodaSimpleUser(String id, String name, String email, boolean guest) {
    super(id, name);
    this.email = email;
    this.guest = guest;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isGuest() {
    return guest;
  }

  public void setGuest(boolean guest) {
    this.guest = guest;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + (guest ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RodaSimpleUser other = (RodaSimpleUser) obj;
    if (email == null) {
      if (other.email != null) {
        return false;
      }
    } else if (!email.equals(other.email)) {
      return false;
    }
    if (guest != other.guest) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RodaSimpleUser [getId()=");
    builder.append(getId());
    builder.append(", getName()=");
    builder.append(getName());
    builder.append(", email=");
    builder.append(email);
    builder.append(", guest=");
    builder.append(guest);
    builder.append("]");
    return builder.toString();
  }

}
