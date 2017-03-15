/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This is a user of RODA.
 *
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_USER)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class User extends RodaPrincipal {
  private static final long serialVersionUID = 6514790636010895870L;

  /** Groups this user belongs to. */
  private Set<String> groups = new HashSet<>();

  /** Email address. */
  private String email;
  /** Is a guest user? */
  private boolean guest;
  /** IP address. */
  private String ipAddress = "";

  /** LDAP info. */
  private String resetPasswordToken = null;
  /** LDAP info. */
  private String resetPasswordTokenExpirationDate = null;
  /** LDAP info. */
  private String emailConfirmationToken = null;
  /** LDAP info. */
  private String emailConfirmationTokenExpirationDate = null;

  /** LDAP description. */
  private String extra = null;

  /**
   * Constructor.
   */
  public User() {
    this((String) null);
  }

  /**
   * Constructs a new user with the given name.
   *
   * @param name
   *          the name of the new user.
   */
  public User(final String name) {
    this(name, name, false);
  }

  public User(final User user) {
    this(user.getId(), user.getName(), user.getFullName(), user.isActive(), user.getAllRoles(), user.getDirectRoles(),
      user.getGroups(), user.getEmail(), user.isGuest(), user.getIpAddress(), user.getExtra(),
      user.getResetPasswordToken(), user.getResetPasswordTokenExpirationDate(), user.getEmailConfirmationToken(),
      user.getEmailConfirmationTokenExpirationDate());
  }

  public User(final String id, final String name, final boolean guest) {
    this(id, name, null, guest);
  }

  public User(final String id, final String name, final String email, final boolean guest) {
    this(id, name, email, guest, "", new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
  }

  public User(final String id, final String name, final String email, final boolean guest, final String ipAddress,
    final Set<String> allRoles, final Set<String> directRoles, final Set<String> groups) {
    this(id, name, email, guest, ipAddress, allRoles, directRoles, groups, null, null, null, null);
  }

  public User(final String id, final String name, final String email, final boolean guest, final String ipAddress,
    final Set<String> allRoles, final Set<String> directRoles, final Set<String> groups,
    final String resetPasswordToken, final String resetPasswordTokenExpirationDate, final String emailConfirmationToken,
    final String emailConfirmationTokenExpirationDate) {
    this(id, name, name, true, allRoles, directRoles, groups, email, guest, ipAddress, null, resetPasswordToken,
      resetPasswordTokenExpirationDate, emailConfirmationToken, emailConfirmationTokenExpirationDate);
  }

  public User(final String id, final String name, final String fullName, final boolean active,
    final Set<String> allRoles, final Set<String> directRoles, final Set<String> groups, final String email,
    final boolean guest, final String ipAddress, final String extra, final String resetPasswordToken,
    final String resetPasswordTokenExpirationDate, final String emailConfirmationToken,
    final String emailConfirmationTokenExpirationDate) {
    super(id, name, fullName, active, allRoles, directRoles);
    this.groups = groups;

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
   * Get {@link User}'s extra information.
   *
   * @return a {@link String} with user's extra information.
   */
  public String getExtra() {
    return extra;
  }

  /**
   * Set {@link User}'s extra information.
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

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(final Set<String> groups) {
    this.groups = groups;
  }

  @Override
  public boolean isUser() {
    return true;
  }

  public void addGroup(final String group) {
    if (groups == null) {
      groups = new HashSet<>();
    }
    groups.add(group);
  }

  public void removeGroup(final String group) {
    if (groups.contains(group)) {
      groups.remove(group);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((emailConfirmationToken == null) ? 0 : emailConfirmationToken.hashCode());
    result = prime * result
      + ((emailConfirmationTokenExpirationDate == null) ? 0 : emailConfirmationTokenExpirationDate.hashCode());
    result = prime * result + ((extra == null) ? 0 : extra.hashCode());
    result = prime * result + ((groups == null) ? 0 : groups.hashCode());
    result = prime * result + (guest ? 1231 : 1237);
    result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
    result = prime * result + ((resetPasswordToken == null) ? 0 : resetPasswordToken.hashCode());
    result = prime * result
      + ((resetPasswordTokenExpirationDate == null) ? 0 : resetPasswordTokenExpirationDate.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null || !super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final User other = (User) obj;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (emailConfirmationToken == null) {
      if (other.emailConfirmationToken != null)
        return false;
    } else if (!emailConfirmationToken.equals(other.emailConfirmationToken))
      return false;
    if (emailConfirmationTokenExpirationDate == null) {
      if (other.emailConfirmationTokenExpirationDate != null)
        return false;
    } else if (!emailConfirmationTokenExpirationDate.equals(other.emailConfirmationTokenExpirationDate))
      return false;
    if (extra == null) {
      if (other.extra != null)
        return false;
    } else if (!extra.equals(other.extra))
      return false;
    if (groups == null) {
      if (other.groups != null)
        return false;
    } else if (!groups.equals(other.groups))
      return false;
    if (guest != other.guest)
      return false;
    if (ipAddress == null) {
      if (other.ipAddress != null)
        return false;
    } else if (!ipAddress.equals(other.ipAddress))
      return false;
    if (resetPasswordToken == null) {
      if (other.resetPasswordToken != null)
        return false;
    } else if (!resetPasswordToken.equals(other.resetPasswordToken))
      return false;
    if (resetPasswordTokenExpirationDate == null) {
      if (other.resetPasswordTokenExpirationDate != null)
        return false;
    } else if (!resetPasswordTokenExpirationDate.equals(other.resetPasswordTokenExpirationDate))
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("User [");
    if (groups != null) {
      builder.append("groups=");
      builder.append(groups);
      builder.append(", ");
    }
    if (email != null) {
      builder.append("email=");
      builder.append(email);
      builder.append(", ");
    }
    builder.append("guest=");
    builder.append(guest);
    builder.append(", ");
    if (ipAddress != null) {
      builder.append("ipAddress=");
      builder.append(ipAddress);
      builder.append(", ");
    }
    if (resetPasswordToken != null) {
      builder.append("resetPasswordToken=");
      builder.append(resetPasswordToken);
      builder.append(", ");
    }
    if (resetPasswordTokenExpirationDate != null) {
      builder.append("resetPasswordTokenExpirationDate=");
      builder.append(resetPasswordTokenExpirationDate);
      builder.append(", ");
    }
    if (emailConfirmationToken != null) {
      builder.append("emailConfirmationToken=");
      builder.append(emailConfirmationToken);
      builder.append(", ");
    }
    if (emailConfirmationTokenExpirationDate != null) {
      builder.append("emailConfirmationTokenExpirationDate=");
      builder.append(emailConfirmationTokenExpirationDate);
      builder.append(", ");
    }
    if (extra != null) {
      builder.append("extra=");
      builder.append(extra);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_NAME);
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }
}
