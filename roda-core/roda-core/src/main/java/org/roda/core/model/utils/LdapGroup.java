package org.roda.core.model.utils;

import java.util.Objects;
import java.util.Set;

import javax.naming.Name;

import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entry(objectClasses = {"extensibleObject", "top", "groupOfUniqueNames"}, base = "")
public class LdapGroup implements Persistable<Name> {

  @Id
  private Name dn;

  @Attribute(name = "cn")
  private String commonName;

  @Attribute(name = "uniqueMember")
  private Set<Name> uniqueMember;

  @Attribute(name = "ou")
  private String ou;

  @Attribute(name = "shadowInactive")
  private String shadowInactive;

  @Transient
  private boolean isNew;

  public Name getId() {
    return dn;
  }

  public void setId(Name dn) {
    this.dn = dn;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public Set<Name> getUniqueMember() {
    return uniqueMember;
  }

  public void setUniqueMember(Set<Name> uniqueMember) {
    this.uniqueMember = uniqueMember;
  }

  public String getOu() {
    return ou;
  }

  public void setOu(String ou) {
    this.ou = ou;
  }

  public String getShadowInactive() {
    return shadowInactive;
  }

  public void setShadowInactive(String shadowInactive) {
    this.shadowInactive = shadowInactive;
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
    LdapGroup ldapGroup = (LdapGroup) o;
    return isNew == ldapGroup.isNew && Objects.equals(dn, ldapGroup.dn)
      && Objects.equals(commonName, ldapGroup.commonName) && Objects.equals(uniqueMember, ldapGroup.uniqueMember)
      && Objects.equals(ou, ldapGroup.ou) && Objects.equals(shadowInactive, ldapGroup.shadowInactive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dn, commonName, uniqueMember, ou, shadowInactive, isNew);
  }

  @Override
  public String toString() {
    return "LdapGroup{" + "dn=" + dn + ", commonName='" + commonName + '\'' + ", uniqueMember='" + uniqueMember + '\''
      + ", ou='" + ou + '\'' + ", shadowInactive='" + shadowInactive + '\'' + ", isNew=" + isNew + '}';
  }
}
