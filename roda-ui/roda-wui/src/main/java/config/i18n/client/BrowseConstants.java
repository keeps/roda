/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public interface BrowseConstants extends Constants, ConstantsWithLookup {

  /** Advanced Search Constants **/

  @DefaultStringValue("january")
  public String january();

  @DefaultStringValue("february")
  public String february();

  @DefaultStringValue("march")
  public String march();

  @DefaultStringValue("april")
  public String april();

  @DefaultStringValue("may")
  public String may();

  @DefaultStringValue("june")
  public String june();

  @DefaultStringValue("july")
  public String july();

  @DefaultStringValue("august")
  public String august();

  @DefaultStringValue("september")
  public String september();

  @DefaultStringValue("october")
  public String october();

  @DefaultStringValue("november")
  public String november();

  @DefaultStringValue("december")
  public String december();

  /** Common Constants **/

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

  @DefaultStringValue("Click here")
  public String focusPanelTitle();

  /** Main constants **/

  // Content titles

  @Key("title.about")
  @DefaultStringValue("Welcome")
  public String title_about();

  @Key("title.dissemination.search.basic")
  @DefaultStringValue("Basic Search")
  public String title_dissemination_search_basic();

  @Key("title.dissemination.browse")
  @DefaultStringValue("Catalogue")
  public String title_dissemination_browse();

  @Key("title.administration")
  @DefaultStringValue("Administration")
  public String title_administration();

  @Key("title.administration.actions")
  @DefaultStringValue("Preservation actions")
  public String title_administration_actions();

  @Key("title.administration.user")
  @DefaultStringValue("User Management")
  public String title_administration_user();

  @Key("title.administration.log")
  @DefaultStringValue("Activity log")
  public String title_administration_log();

  @Key("title.administration.preferences")
  @DefaultStringValue("Preferences")
  public String title_administration_preferences();

  @Key("title.planning")
  @DefaultStringValue("Planning")
  public String title_planning();

  @Key("title.planning.monitoring")
  @DefaultStringValue("Internal monitoring")
  public String title_planning_monitoring();

  @Key("title.planning.risk")
  @DefaultStringValue("Risk register")
  public String title_planning_risk();

  @Key("title.planning.agents")
  @DefaultStringValue("Agent register")
  public String title_planning_agent();

  @Key("title.planning.format")
  @DefaultStringValue("Format register")
  public String title_planning_format();

  @Key("title.ingest")
  @DefaultStringValue("Ingest")
  public String title_ingest();

  @Key("title.ingest.preIngest")
  @DefaultStringValue("Pre-ingest")
  public String title_ingest_pre();

  @Key("title.ingest.transfer")
  @DefaultStringValue("Transfer")
  public String title_ingest_transfer();

  @Key("title.ingest.list")
  @DefaultStringValue("Process")
  public String title_ingest_list();

  @Key("title.ingest.appraisal")
  @DefaultStringValue("Appraisal")
  public String title_ingest_appraisal();

  @Key("title.settings")
  @DefaultStringValue("Settings")
  public String title_settings();

  // Login Panel

  @DefaultStringValue("Login")
  public String loginLogin();

  @DefaultStringValue("Sign up")
  public String loginRegister();

  @DefaultStringValue("Profile")
  public String loginProfile();

  @DefaultStringValue("Logout")
  public String loginLogout();

  // Login Dialog
  @DefaultStringValue("Authentication")
  public String loginDialogTitle();

  @DefaultStringValue("Login")
  public String loginDialogLogin();

  @DefaultStringValue("Cancel")
  public String loginDialogCancel();

  // Home
  @DefaultStringValue("Go Home")
  public String homeTitle();

  // Content Panel
  @DefaultStringValue("Authorization Denied")
  public String authorizationDeniedAlert();

  @DefaultStringValue("You need to be authenticated to access this page. Do you want to authenticate?")
  public String casForwardWarning();

  // Cookies
  @DefaultStringValue("This website uses cookies to ensure you get the best experience on our website. ")
  public String cookiesMessage();

  @DefaultStringValue("Got it!")
  public String cookiesDismisse();

  @DefaultStringValue("Learn more")
  public String cookiesLearnMore();

  /** Metadata editor constants **/

  // Move element
  @DefaultStringValue("Choose the destination")
  public String moveChooseDestinationTitle();

  @DefaultStringValue("MOVE")
  public String moveChooseDestinationChoose();

  @DefaultStringValue("CANCEL")
  public String moveChooseDestinationCancel();

  @DefaultStringValue("The element has been successfully moved")
  public String moveSuccessful();

  // Edit Producers Panel
  @DefaultStringValue("Users or groups:")
  public String editProducersTitle();

  @DefaultStringValue("User")
  public String editProducersAddUser();

  @DefaultStringValue("Group")
  public String editProducersAddGroup();

  @DefaultStringValue("Producer")
  public String editProducersDelete();

  @DefaultStringValue("RODA-in")
  public String editProducersRodaIn();

  // Edit Object Permissions Panel
  @DefaultStringValue("Apply recursively to all sub-levels")
  public String objectPermissionsApplyRecursivelly();

  @DefaultStringValue("User")
  public String objectPermissionsAddUser();

  @DefaultStringValue("Group")
  public String objectPermissionsAddGroup();

  @DefaultStringValue("Save")
  public String objectPermissionsSave();

  // Meta permissions
  @DefaultStringValue("No Access")
  public String permission_object_NoAccess();

  @DefaultStringValue("Access to metadata and disseminations")
  public String permission_object_ReadOnly();

  @DefaultStringValue("Access and metadata edition")
  public String permission_object_ReadAndEditMetadata();

  @DefaultStringValue("Full Control")
  public String permission_object_FullControl();

}
