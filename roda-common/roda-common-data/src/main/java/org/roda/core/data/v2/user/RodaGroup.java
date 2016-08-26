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

public class RodaGroup extends RodaPrincipal implements RODAMember {
  private static final long serialVersionUID = -718342371831706371L;

  public RodaGroup() {
    this(null, null);
  }

  public RodaGroup(final String id, final String name) {
    this(id, name, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
  }

  public RodaGroup(final String id, final String name, final Set<String> allRoles, final Set<String> directRoles,
    final Set<String> allGroups, final Set<String> directGroups) {
    super(id, name, allRoles, directRoles, allGroups, directGroups);
  }

  @Override
  public boolean isUser() {
    return false;
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
    return true;
  }

  @Override
  public String toString() {
    return "RodaGroup [" + super.toString() + "]";
  }

}
