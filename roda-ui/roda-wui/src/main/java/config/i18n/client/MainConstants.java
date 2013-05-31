package config.i18n.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria Interface to represent the constants contained in resource
 *         bundle: 'MainConstants.properties'.
 */
public interface MainConstants extends Constants, ConstantsWithLookup {

	// Content titles

	@Key("title.home")
	@DefaultStringValue("Home")
	public String title_home();

	@Key("title.help.supportsoftware")
	@DefaultStringValue("Support Software")
	public String title_help_supportsoftware();

	@Key("title.preferences")
	@DefaultStringValue("Preferences")
	public String title_preferences();

	@Key("title.register")
	@DefaultStringValue("Register")
	public String title_register();

	@Key("title.verifyEmail")
	@DefaultStringValue("Verify e-mail")
	public String title_verifyemail();

	@Key("title.recoverLogin")
	@DefaultStringValue("Recover Login")
	public String title_recoverLogin();

	@Key("title.resetpassword")
	@DefaultStringValue("Reset password")
	public String title_resetpassword();

	@Key("title.about")
	@DefaultStringValue("About")
	public String title_about();
	
	@Key("title.about.services")
	@DefaultStringValue("Services")
	public String title_about_services();

	@Key("title.about.policies")
	@DefaultStringValue("Policies")
	public String title_about_policies();

	@Key("title.about.researchDevelopment")
	@DefaultStringValue("I&D")
	public String title_about_researchDevelopment();
	
	@Key("title.about.contacts")
	@DefaultStringValue("Contacts")
	public String title_about_contacts();

	@Key("title.about.register")
	@DefaultStringValue("Register")
	public String title_about_register();
	
	@Key("title.about.help")
	@DefaultStringValue("Help")
	public String title_about_help();

	@Key("title.dissemination")
	@DefaultStringValue("Dissemination")
	public String title_dissemination();

	@Key("title.dissemination.search")
	@DefaultStringValue("Search")
	public String title_dissemination_search();

	@Key("title.dissemination.search.basic")
	@DefaultStringValue("Basic Search")
	public String title_dissemination_search_basic();

	
	@Key("title.dissemination.search.advanced")
	@DefaultStringValue("Advanced Search")
	public String title_dissemination_search_advanced();
	
	@Key("title.dissemination.help")
	@DefaultStringValue("Help")
	public String title_dissemination_help();

	@Key("title.dissemination.browse")
	@DefaultStringValue("Browse")
	public String title_dissemination_browse();

	@Key("title.administration")
	@DefaultStringValue("Administration")
	public String title_administration();

	@Key("title.administration.user")
	@DefaultStringValue("User Management")
	public String title_administration_user();

	@Key("title.administration.user.users")
	@DefaultStringValue("User list")
	public String title_administration_user_users();

	@Key("title.administration.user.groups")
	@DefaultStringValue("Group list")
	public String title_administration_user_groups();

	@Key("title.administration.event")
	@DefaultStringValue("Scheduler")
	public String title_administration_event();

	@Key("title.administration.event.tasks")
	@DefaultStringValue("Scheduler list")
	public String title_administration_event_tasks();

	@Key("title.administration.event.taskInstances")
	@DefaultStringValue("Scheduler historic")
	public String title_administration_event_taskInstances();

	@Key("title.administration.metadataEditor")
	@DefaultStringValue("Metadata Editor")
	public String title_administration_metadataEditor();
	
	@Key("title.administration.statistics")
	@DefaultStringValue("Statistics")
	public String title_administration_statistics();
	
	@Key("title.administration.log")
	@DefaultStringValue("Log")
	public String title_administration_log();
	
	@Key("title.administration.help")
	@DefaultStringValue("Help")
	public String title_administration_help();

	@Key("title.ingest")
	@DefaultStringValue("Ingest")
	public String title_ingest();

	@Key("title.ingest.preIngest")
	@DefaultStringValue("Pre-ingest")
	public String title_ingest_pre();

	@Key("title.ingest.submit")
	@DefaultStringValue("Submit")
	public String title_ingest_submit();

	@Key("title.ingest.submit.upload")
	@DefaultStringValue("Upload")
	public String title_ingest_submit_upload();

	@Key("title.ingest.submit.create")
	@DefaultStringValue("Create")
	public String title_ingest_submit_create();

	@Key("title.ingest.list")
	@DefaultStringValue("Status")
	public String title_ingest_list();
	
	@Key("title.ingest.help")
	@DefaultStringValue("Help")
	public String title_ingest_help();

	// Login Panel

	@DefaultStringValue("Welcome")
	public String loginVisitorMessage();

	@DefaultStringValue("Username")
	public String loginUsername();

	@DefaultStringValue("Password")
	public String loginPassword();

	@DefaultStringValue("Preferences")
	public String loginPreferences();

	@DefaultStringValue("Logout")
	public String loginLogout();

	// Login Dialog
	@DefaultStringValue("Authentication")
	public String loginDialogTitle();

	@DefaultStringValue("Username")
	public String loginDialogUsername();

	@DefaultStringValue("Password")
	public String loginDialogPassword();

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


}
