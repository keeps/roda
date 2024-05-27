package org.roda.core.model.utils;

import java.util.Objects;
import java.util.Set;

import javax.naming.Name;

import org.roda.core.data.v2.generics.MetadataValue;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entry(objectClasses = {"extensibleObject", "top", "person", "organizationalPerson", "inetOrgPerson",
  "shadowAccount"}, base = "")
public final class LdapUser implements Persistable<Name> {

  @Id
  private Name dn;

  @Attribute(name = "cn")
  private String commonName;

  @Attribute(name = "sn")
  private String surname;

  @Attribute(name = "email")
  private String email;

  @Attribute(name = "givenName")
  private String givenName;

  @Attribute(name = "uid")
  private String uid;

  @Attribute(name = "userPassword")
  private String userPassword;

  @Attribute(name = "shadowInactive")
  private String shadowInactive;

  @Attribute(name = "description")
  private String description;

  @Attribute(name = "info")
  private String info;

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

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  public String getShadowInactive() {
    return shadowInactive;
  }

  public void setShadowInactive(String shadowInactive) {
    this.shadowInactive = shadowInactive;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
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
    LdapUser ldapUser = (LdapUser) o;
    return isNew == ldapUser.isNew && Objects.equals(dn, ldapUser.dn) && Objects.equals(commonName, ldapUser.commonName)
      && Objects.equals(surname, ldapUser.surname) && Objects.equals(email, ldapUser.email)
      && Objects.equals(givenName, ldapUser.givenName) && Objects.equals(uid, ldapUser.uid)
      && Objects.equals(userPassword, ldapUser.userPassword) && Objects.equals(shadowInactive, ldapUser.shadowInactive)
      && Objects.equals(description, ldapUser.description) && Objects.equals(info, ldapUser.info);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dn, commonName, surname, email, givenName, uid, userPassword, shadowInactive, description, info,
      isNew);
  }

  @Override
  public String toString() {
    return "LdapUser{" + "dn=" + dn + ", commonName='" + commonName + '\'' + ", surname='" + surname + '\''
      + ", email='" + email + '\'' + ", givenName='" + givenName + '\'' + ", uid='" + uid + '\'' + ", userPassword='"
      + userPassword + '\'' + ", shadowInactive=" + shadowInactive + ", description='" + description + '\'' + ", info='"
      + info + '\'' + ", isNew=" + isNew + '}';
  }
}
