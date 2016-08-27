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

/**
 * This is a user of RODA.
 *
 * @author Rui Castro
 */
public class RodaSimpleUser extends RodaPrincipal {
  private static final long serialVersionUID = 6514790636010895870L;

  /** Email address */
  private String email;
  /** Is a guest user? */
  private boolean guest;
  /** IP address */
  private String ipAddress = "";

  /** LDAP info */
  private String resetPasswordToken = null;
  /** LDAP info */
  private String resetPasswordTokenExpirationDate = null;
  /** LDAP info */
  private String emailConfirmationToken = null;
  /** LDAP info */
  private String emailConfirmationTokenExpirationDate = null;

  /** LDAP description */
  private String extra = null;

  /**
   * Constructor.
   */
  public RodaSimpleUser() {
    this((String) null);
  }

  /**
   * Constructs a new user with the given name.
   *
   * @param name
   *          the name of the new user.
   */
  public RodaSimpleUser(final String name) {
    this(name, name, false);
    setActive(true);
  }

  public RodaSimpleUser(final RodaSimpleUser user) {
    this(user.getId(), user.getName(), user.getFullName(), user.isActive(), user.getAllRoles(), user.getDirectRoles(),
      user.getAllGroups(), user.getDirectGroups(), user.getEmail(), user.isGuest(), user.getIpAddress(),
      user.getExtra(), user.getResetPasswordToken(), user.getResetPasswordTokenExpirationDate(),
      user.getEmailConfirmationToken(), user.getEmailConfirmationTokenExpirationDate());
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
    this(id, name, email, guest, ipAddress, allRoles, directRoles, allGroups, directGroups, null, null, null, null);
  }

  public RodaSimpleUser(final String id, final String name, final String email, final boolean guest,
    final String ipAddress, final Set<String> allRoles, final Set<String> directRoles, final Set<String> allGroups,
    final Set<String> directGroups, final String resetPasswordToken, final String resetPasswordTokenExpirationDate,
    final String emailConfirmationToken, final String emailConfirmationTokenExpirationDate) {
    this(id, name, name, true, allRoles, directRoles, allGroups, directGroups, email, guest, ipAddress, null,
      resetPasswordToken, resetPasswordTokenExpirationDate, emailConfirmationToken,
      emailConfirmationTokenExpirationDate);
  }

  public RodaSimpleUser(final String id, final String name, final String fullName, final boolean active,
    final Set<String> allRoles, final Set<String> directRoles, final Set<String> allGroups,
    final Set<String> directGroups, final String email, final boolean guest, final String ipAddress, final String extra,
    final String resetPasswordToken, final String resetPasswordTokenExpirationDate, final String emailConfirmationToken,
    final String emailConfirmationTokenExpirationDate) {
    super(id, name, fullName, active, allRoles, directRoles, allGroups, directGroups);
    this.email = email;
    this.guest = guest;
    this.ipAddress = ipAddress;
    this.extra = extra;
    this.resetPasswordToken = resetPasswordToken;
    this.resetPasswordTokenExpirationDate = resetPasswordTokenExpirationDate;
    this.emailConfirmationToken = emailConfirmationToken;
    this.emailConfirmationTokenExpirationDate = emailConfirmationTokenExpirationDate;
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

  /**
   * Get {@link RodaSimpleUser}'s extra information.
   *
   * @return a {@link String} with user's extra information.
   */
  public String getExtra() {
    return extra;
  }

  /**
   * Set {@link RodaSimpleUser}'s extra information.
   *
   * @param extra
   *          a {@link String} with user's extra information.
   */
  public void setExtra(final String extra) {
    this.extra = extra;
  }

  /**
   * @return the resetPasswordToken
   */
  public String getResetPasswordToken() {
    return resetPasswordToken;
  }

  /**
   * @param resetPasswordToken
   *          the resetPasswordToken to set
   */
  public void setResetPasswordToken(final String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }

  /**
   * @return the resetPasswordTokenExpirationDate
   */
  public String getResetPasswordTokenExpirationDate() {
    return resetPasswordTokenExpirationDate;
  }

  /**
   * @param resetPasswordTokenExpirationDate
   *          the resetPasswordTokenExpirationDate to set
   */
  public void setResetPasswordTokenExpirationDate(final String resetPasswordTokenExpirationDate) {
    this.resetPasswordTokenExpirationDate = resetPasswordTokenExpirationDate;
  }

  /**
   * @return the emailConfirmationToken
   */
  public String getEmailConfirmationToken() {
    return emailConfirmationToken;
  }

  /**
   * @param emailConfirmationToken
   *          the emailConfirmationToken to set
   */
  public void setEmailConfirmationToken(final String emailConfirmationToken) {
    this.emailConfirmationToken = emailConfirmationToken;
  }

  /**
   * @return the emailConfirmationTokenExpirationDate
   */
  public String getEmailConfirmationTokenExpirationDate() {
    return emailConfirmationTokenExpirationDate;
  }

  /**
   * @param emailConfirmationTokenExpirationDate
   *          the emailConfirmationTokenExpirationDate to set
   */
  public void setEmailConfirmationTokenExpirationDate(final String emailConfirmationTokenExpirationDate) {
    this.emailConfirmationTokenExpirationDate = emailConfirmationTokenExpirationDate;
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
    builder.append("RodaSimpleUser [");
    builder.append(super.toString());
    builder.append(", email=");
    builder.append(email);
    builder.append(", guest=");
    builder.append(guest);
    builder.append(", ipAddress=");
    builder.append(ipAddress);
    builder.append(", resetPasswordToken=");
    builder.append(resetPasswordToken);
    builder.append(", resetPasswordTokenExpirationDate=");
    builder.append(resetPasswordTokenExpirationDate);
    builder.append(", emailConfirmationToken=");
    builder.append(emailConfirmationToken);
    builder.append(", emailConfirmationTokenExpirationDate=");
    builder.append(emailConfirmationTokenExpirationDate);
    builder.append(", extra=");
    builder.append(extra);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public boolean isUser() {
    return true;
  }
}
