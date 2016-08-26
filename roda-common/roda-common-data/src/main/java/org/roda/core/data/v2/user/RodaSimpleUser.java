/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.HashSet;
import java.util.Set;

public class RodaSimpleUser extends RodaPrincipal {
  private static final long serialVersionUID = 6514790636010895870L;

  private String email;
  private boolean guest;
  private String ipAddress = "";

  public RodaSimpleUser() {
    this(null, null, null, false);
  }

  public RodaSimpleUser(final RodaSimpleUser user) {
    this(user.getId(), user.getName(), user.getEmail(), user.isGuest(), user.getIpAddress(), user.getAllRoles(),
      user.getDirectRoles(), user.getAllGroups(), user.getDirectGroups());
  }

  public RodaSimpleUser(final String id, final String name, final boolean guest) {
    this(id, name, null, guest);
  }

  public RodaSimpleUser(final String id, final String name, final String email, final boolean guest) {
    this(id, name, email, guest, "", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(),
      new HashSet<String>());
  }

  public RodaSimpleUser(final String id, final String name, final String email, final boolean guest,
    final String ipAddress, final Set<String> allRoles, final Set<String> directRoles, final Set<String> allGroups,
    final Set<String> directGroups) {
    super(id, name, allRoles, directRoles, allGroups, directGroups);
    this.email = email;
    this.guest = guest;
    this.ipAddress = ipAddress;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public boolean isGuest() {
    return guest;
  }

  public void setGuest(final boolean guest) {
    this.guest = guest;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RodaSimpleUser other = (RodaSimpleUser) obj;
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
    if (ipAddress == null) {
      if (other.ipAddress != null) {
        return false;
      }
    } else if (!ipAddress.equals(other.ipAddress)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("RodaSimpleUser [getId()=");
    builder.append(getId());
    builder.append(", getName()=");
    builder.append(getName());
    builder.append(", email=");
    builder.append(email);
    builder.append(", guest=");
    builder.append(guest);
    builder.append(", ipAddress=");
    builder.append(ipAddress);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public boolean isUser() {
    return true;
  }
}
