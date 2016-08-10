/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.Arrays;
import java.util.Date;

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

  // LDAP documentTitle
  private String idDocumentType = null;
  // LDAP documentIdentifier
  private String idDocument = null;
  // LDAP documentLocation
  private String idDocumentLocation = null;
  // LDAP documentVersion
  private Date idDocumentDate = null;

  // LDAP serialNumber
  private String financeIdentificationNumber = null;

  // LDAP friendlyCountryName
  private String birthCountry = null;

  // LDAP postalAddress
  private String postalAddress = null;
  // LDAP postalCode
  private String postalCode = null;
  // LDAP l
  private String localityName = null;
  // LDAP c
  private String countryName = null;
  // LDAP telephoneNumber
  private String telephoneNumber = null;
  // LDAP fax
  private String fax = null;
  // // LDAP email
  // private String email = null;

  // LDAP businessCategory
  private String businessCategory = null;

  // LDAP info
  private String resetPasswordToken = null;
  // LDAP info
  private String resetPasswordTokenExpirationDate = null;
  // LDAP info
  private String emailConfirmationToken = null;
  // LDAP info
  private String emailConfirmationTokenExpirationDate = null;

  /** Extra information about this user. */
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
    setIdDocumentType(user.getIdDocumentType());
    setIdDocument(user.getIdDocument());
    setIdDocumentLocation(user.getIdDocumentLocation());
    setIdDocumentDate(user.getIdDocumentDate());

    setFinanceIdentificationNumber(user.getFinanceIdentificationNumber());
    setBirthCountry(user.getBirthCountry());

    setPostalAddress(user.getPostalAddress());
    setPostalCode(user.getPostalCode());
    setLocalityName(user.getLocalityName());
    setCountryName(user.getCountryName());
    setTelephoneNumber(user.getTelephoneNumber());
    setFax(user.getFax());
    setEmail(user.getEmail());

    setBusinessCategory(user.getBusinessCategory());

    setResetPasswordToken(user.getResetPasswordToken());
    setResetPasswordTokenExpirationDate(user.getResetPasswordTokenExpirationDate());
    setEmailConfirmationToken(user.getResetPasswordToken());
    setEmailConfirmationTokenExpirationDate(user.getResetPasswordTokenExpirationDate());
  }

  public User(final RodaUser rodaUser) {
    super(rodaUser);
  }

  /**
   * @see RODAMember#toString()
   */
  public String toString() {
    return "User (" + super.toString() + ", idDocumentType=" + getIdDocumentType() + ", idDocument=" + getIdDocument()
      + ", idDocumentLocation=" + getIdDocumentLocation() + ", idDocumentDate=" + getIdDocumentDate()
      + ", financeIdentificationNumber=" + getFinanceIdentificationNumber() + ", birthCountry=" + getBirthCountry()
      + ", postalAddress=" + getPostalAddress() + ", postalCode=" + getPostalCode() + ", localityName="
      + getLocalityName() + ", countryName=" + getCountryName() + ", telephoneName=" + getTelephoneNumber() + ", fax="
      + getFax() + ", email=" + getEmail() + ", businessCategory=" + getBusinessCategory() + ", extra=" + getExtra()
      + ", resetPasswordToken=" + getResetPasswordToken() + ", resetPasswordTokenExpirationDate="
      + getResetPasswordTokenExpirationDate() + ", emailConfirmationToken=" + getEmailConfirmationToken()
      + ", emailConfirmationTokenExpirationDate=" + getEmailConfirmationTokenExpirationDate() + ")";
  }

  /**
   * @return the idDocumentType
   */
  public String getIdDocumentType() {
    return idDocumentType;
  }

  /**
   * @param idDocumentType
   *          the idDocumentType to set
   */
  public void setIdDocumentType(String idDocumentType) {

    if (idDocumentType == null || Arrays.asList(ID_TYPES).contains(idDocumentType.toLowerCase())) {

      this.idDocumentType = idDocumentType;

    } else {
      throw new IllegalArgumentException("'" + idDocumentType + "' is not a valid ID document type");
    }
  }

  /**
   * @return the idDocument
   */
  public String getIdDocument() {
    return idDocument;
  }

  /**
   * @param idDocument
   *          the idDocument to set
   */
  public void setIdDocument(String idDocument) {
    this.idDocument = idDocument;
  }

  /**
   * @return the idDocumentLocation
   */
  public String getIdDocumentLocation() {
    return idDocumentLocation;
  }

  /**
   * @param idDocumentLocation
   *          the idDocumentLocation to set
   */
  public void setIdDocumentLocation(String idDocumentLocation) {
    this.idDocumentLocation = idDocumentLocation;
  }

  /**
   * @return the idDocumentDate
   */
  public Date getIdDocumentDate() {
    return idDocumentDate;
  }

  /**
   * @param idDocumentDate
   *          the idDocumentDate to set
   */
  public void setIdDocumentDate(Date idDocumentDate) {
    this.idDocumentDate = idDocumentDate;
  }

  /**
   * @return the financeIdentificationNumber
   */
  public String getFinanceIdentificationNumber() {
    return financeIdentificationNumber;
  }

  /**
   * @param financeIdentificationNumber
   *          the financeIdentificationNumber to set
   */
  public void setFinanceIdentificationNumber(String financeIdentificationNumber) {
    this.financeIdentificationNumber = financeIdentificationNumber;
  }

  /**
   * @return the birthCountry
   */
  public String getBirthCountry() {
    return birthCountry;
  }

  /**
   * @param birthCountry
   *          the birthCountry to set
   */
  public void setBirthCountry(String birthCountry) {
    this.birthCountry = birthCountry;
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
   * 
   * @return the telephone number
   */
  public String getTelephoneNumber() {
    return telephoneNumber;
  }

  /**
   * 
   * @param telephoneNumber
   *          telephone number
   */
  public void setTelephoneNumber(String telephoneNumber) {
    this.telephoneNumber = telephoneNumber;
  }

  /**
   * 
   * @return the postal address
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * @param postalAddress
   *          the postal address
   */
  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * @return the postal code
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * @param postalCode
   *          the postal code to set
   */
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  /**
   * @return the locality name
   */
  public String getLocalityName() {
    return localityName;
  }

  /**
   * @param localityName
   *          the locality name to set
   */
  public void setLocalityName(String localityName) {
    this.localityName = localityName;
  }

  /**
   * @return the country name
   */
  public String getCountryName() {
    return countryName;
  }

  /**
   * @param countryName
   *          the country name to set
   */
  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  /**
   * @return the fax
   */
  public String getFax() {
    return fax;
  }

  /**
   * @param fax
   *          the fax to set
   */
  public void setFax(String fax) {
    this.fax = fax;
  }

  /**
   * @return the businessCategory
   */
  public String getBusinessCategory() {
    return businessCategory;
  }

  /**
   * @param businessCategory
   *          the businessCategory to set
   */
  public void setBusinessCategory(String businessCategory) {
    this.businessCategory = businessCategory;
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

}
