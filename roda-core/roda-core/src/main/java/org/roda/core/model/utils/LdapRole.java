package org.roda.core.model.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.naming.Name;

import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entry(objectClasses = {"top", "organizationalRole"}, base = "")
public final class LdapRole implements Persistable<Name> {

  @Id
  private Name dn;

  @Attribute(name = "cn")
  private String commonName;

  @Attribute(name = "roleOccupant")
  private List<Name> roleOccupants;

  @Transient
  private boolean isNew;

  @Override
  public Name getId() {
    return getDn();
  }

  public Name getDn() {
    return dn;
  }

  public void setDn(Name dn) {
    this.dn = dn;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public List<Name> getRoleOccupants() {
    return roleOccupants;
  }

  public void setRoleOccupants(List<Name> roleOccupants) {
    this.roleOccupants = roleOccupants;
  }

  public void addRoleOccupant(Name roleOccupant) {
    if (roleOccupants == null) {
      this.roleOccupants = new ArrayList<>();
    }
    this.roleOccupants.add(roleOccupant);
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean aNew) {
    isNew = aNew;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    LdapRole ldapRole = (LdapRole) o;
    return isNew == ldapRole.isNew && Objects.equals(dn, ldapRole.dn) && Objects.equals(commonName, ldapRole.commonName)
      && Objects.equals(roleOccupants, ldapRole.roleOccupants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dn, commonName, roleOccupants, isNew);
  }

  @Override
  public String toString() {
    return "LdapRole{" + "dn=" + dn + ", commonName='" + commonName + '\'' + ", roleOccupants=" + roleOccupants
      + ", isNew=" + isNew + '}';
  }
}
