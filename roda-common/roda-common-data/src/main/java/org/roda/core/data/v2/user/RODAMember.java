/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.Set;

import org.roda.core.data.v2.index.IsIndexed;

public interface RODAMember extends IsIndexed {

  boolean isActive();

  boolean isUser();

  String getId();

  String getName();

  String getFullName();

  Set<String> getAllGroups();

  Set<String> getDirectGroups();

  Set<String> getAllRoles();

  Set<String> getDirectRoles();

  boolean isNameValid();

}
