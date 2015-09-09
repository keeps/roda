/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Luis Faria
 * 
 */
public interface CommonConstants extends Constants {

  // Locale
  @DefaultStringValue("en")
  public String locale();

  // Alphabet Sorted List
  @DefaultStringValue("ALL")
  public String AlphabetSortedListAll();

  // User Info Panel
  @DefaultStringValue("Details")
  public String userInfoDetails();

  @DefaultStringValue("Full Name")
  public String userInfoFullname();

  @DefaultStringValue("Business category")
  public String userInfoBusinessCategory();

  @DefaultStringValue("Organization")
  public String userInfoOrganization();

  @DefaultStringValue("Email")
  public String userInfoEmail();

  @DefaultStringValue("Telephone number")
  public String userInfoTelephoneNumber();

  @DefaultStringValue("Fax")
  public String userInfoFax();

  @DefaultStringValue("Address")
  public String userInfoPostalAddress();

  @DefaultStringValue("Postcode")
  public String userInfoPostalCode();

  @DefaultStringValue("City")
  public String userInfoLocality();

  @DefaultStringValue("Country")
  public String userInfoCountry();

  // Logger
  @DefaultStringValue("Error")
  public String alertErrorTitle();

  // Lazy Vertical List
  @DefaultStringValue("loading list")
  public String lazyListLoading();

  @DefaultStringValue("updating list")
  public String lazyListUpdating();

  @DefaultStringValue("reseting list")
  public String lazyListReseting();

  @DefaultStringValue("printing list")
  public String lazyListPrinting();

  @DefaultStringValue("Export the current list to PDF")
  public String lazyListPrintPDF();

  @DefaultStringValue("Export the current list to CSV")
  public String lazyListPrintCSV();

  // Report Window
  @DefaultStringValue("CLOSE")
  public String reportWindowClose();

  @DefaultStringValue("PDF")
  public String reportWindowPrintPDF();

  @DefaultStringValue("CSV")
  public String reportWindowPrintCSV();

  // ID Type
  @DefaultStringValue("Simple identifier")
  public String simpleID();

  @DefaultStringValue("Full identifier")
  public String fullID();

  // Redaction Type
  @DefaultStringValue("Input")
  public String input();

  @DefaultStringValue("Output")
  public String output();

  // Accessibility
  
  @DefaultStringValue("Click here")
  public String focusPanelTitle();

}
