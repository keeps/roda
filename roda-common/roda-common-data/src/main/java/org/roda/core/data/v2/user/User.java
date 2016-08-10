/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

/**
 * This is a user of RODA.
 * 
 * @author Rui Castro
 */
public class User extends RodaUser {

  private static final long serialVersionUID = 4646063259443634000L;

  public static final String ID_TYPE_BI = "bi";
  public static final String ID_TYPE_PASSPORT = "passport";
  public static final String ID_TYPE_CITIZEN_CARD = "citizen_card";

  // FIXME change this to list?
  public static final String[] ID_TYPES = new String[] {ID_TYPE_BI, ID_TYPE_PASSPORT, ID_TYPE_CITIZEN_CARD};

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
   * Constructs a new empty user.
   */
  public User() {
    super();
  }

  /**
   * Constructs a new user with the given name.
   * 
   * @param name
   *          the name of the new user.
   */
  public User(final String name) {
    super();
    super.setName(name);
    super.setId(name);
    setActive(true);
  }

  /**
   * Constructs a new user cloning a given user.
   * 
   * @param user
   *          the User to be cloned.
   */
  public User(final User user) {
    super(user.getId(), user.getName(), user.getEmail(), false, user.getIpAddress(), user.getDirectGroups(),
      user.getAllGroups(), user.getDirectRoles(), user.getAllRoles());
    setActive(user.isActive());
    setEmail(user.getEmail());
    setExtra(user.getExtra());
    setResetPasswordToken(user.getResetPasswordToken());
    setResetPasswordTokenExpirationDate(user.getResetPasswordTokenExpirationDate());
    setEmailConfirmationToken(user.getResetPasswordToken());
    setEmailConfirmationTokenExpirationDate(user.getResetPasswordTokenExpirationDate());
  }

  /**
   * Construct a new User cloning a given {@link RodaUser}
   * 
   * @param rodaUser
   *          the {@link RodaUser} to be cloned.
   */
  public User(final RodaUser rodaUser) {
    super(rodaUser);
  }

  /**
   * @see RODAMember#toString()
   */
  public String toString() {
    return "User (" + super.toString() + ", email=" + getEmail() + ", extra=" + getExtra() + ", resetPasswordToken="
      + getResetPasswordToken() + ", resetPasswordTokenExpirationDate=" + getResetPasswordTokenExpirationDate()
      + ", emailConfirmationToken=" + getEmailConfirmationToken() + ", emailConfirmationTokenExpirationDate="
      + getEmailConfirmationTokenExpirationDate() + ")";
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return super.getEmail();
  }

  /**
   * @param email
   *          the email to set
   */
  public void setEmail(final String email) {
    super.setEmail(email);
  }

  /**
   * Get User's extra information.
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

}
