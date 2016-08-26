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

public class RodaUser extends RodaSimpleUser implements RODAMember {
  private static final long serialVersionUID = -718342371831706371L;

  public RodaUser() {
    super();
  }

  public RodaUser(final RodaSimpleUser user) {
    this(user.getId(), user.getName(), user.getEmail(), user.isGuest(), user.getIpAddress(), user.getAllGroups(),
      user.getDirectGroups(), user.getAllGroups(), user.getDirectGroups());
  }

  public RodaUser(final String id, final String name, final String email, final boolean guest) {
    this(id, name, email, guest, "", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
  }

  public RodaUser(final String id, final String name, final String email, final boolean guest, final String ipAddress,
    final Set<String> allRoles, final Set<String> directRoles, final Set<String> allGroups,
    final Set<String> directGroups) {
    super(id, name, email, guest, ipAddress, allRoles, directRoles, allGroups, directGroups);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof RodaUser)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RodaUser [" + super.toString() + "]";
  }

}
