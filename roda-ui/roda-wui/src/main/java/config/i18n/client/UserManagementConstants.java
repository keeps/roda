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
 * 
 *         Interface to represent the constants contained in resource bundle:
 *         'UserManagementConstants.properties'.
 */
public interface UserManagementConstants extends Constants, ConstantsWithLookup {

  // Control Panel
  public String list();

  public String users();

  public String groups();

  public String search();

  public String userActions();

  public String report();

  public String createUser();

  public String editUser();

  public String removeUser();

  public String setActive();

  public String setInactive();

  public String groupActions();

  public String createGroup();

  public String editGroup();

  public String removeGroup();

  // Select user or group
  public String selectNoUser();

  public String selectNoGroup();

  public String selectNoActiveUser();

  public String selectNoInactiveUser();

  public String selectNoUserOrGroup();

  // Create/Edit User/Group
  public String dataTabTitle();

  public String permissionsTabTitle();

  // * Create User
  public String createUserTitle();

  public String createUserCancel();

  public String createUserCreate();

  // ** Create User Data

  public String username();

  public String password();

  public String passwordNote();

  public String userDataChangePassword();

  public String fullname();

  public String jobFunction();

  public String[] getJobFunctions();

  public String idTypeAndNumber();

  public String id_type_bi();

  public String id_type_passport();

  public String id_type_citizen_card();

  public String idDateAndLocality();

  public String nationality();

  public String[] nationalityList();

  public String address();

  public String postalCodeAndLocality();

  public String country();

  public String[] countryList();

  public String nif();

  public String email();

  public String phonenumber();

  public String fax();

  public String userDataNote();

  public String allGroups();

  public String memberGroups();

  // * Edit User
  public String editUserTitle();

  public String editUserCancel();

  public String editUserApply();

  public String editUserData();

  public String editUserPermissions();

  // * Create Group
  public String createGroupTitle();

  public String createGroupCreate();

  public String createGroupCancel();

  public String groupName();

  public String groupFullname();

  // * Edit Group
  public String editGroupTitle();

  public String editGroupApply();

  public String editGroupCancel();

  // * Roles Description
  public String role_01();

  public String role_02();

  public String role_03();

  public String role_04();

  public String role_05();

  public String role_06();

  public String role_07();

  public String role_08();

  public String role_09();

  public String role_10();

  public String role_11();

  public String role_12();

  public String role_13();

  public String role_14();

  public String role_15();

  public String role_16();

  public String role_17();

  // Action Report Window
  public String actionReportClose();

  public String actionReportLogTabTitle();

  // User log
  public String actionReportLogDateTime();

  public String actionReportLogAction();

  public String actionReportLogParameters();

  public String actionReportLogUser();

  public String userlog_initialDate();

  public String userlog_finalDate();

  public String userlog_setFilter();

  public String userlog_actions();

  public String userlog_allActions();

  // Register
  public String registerUserDataTitle();

  public String registerDisclaimer();

  public String registerCaptchaTitle();

  public String registerSubmit();

  public String registerUserExists();

  public String registerEmailAlreadyExists();

  public String registerSuccess();

  public String registerWrongCaptcha();

  // Verify email
  public String verifyEmailUsername();

  public String verifyEmailToken();

  public String verifyEmailVerify();

  public String verifyEmailResend();

  public String verifyEmailChange();

  public String verifyEmailNoSuchUser();

  public String verifyEmailSuccess();

  public String verifyEmailWrongToken();

  public String verifyEmailResendSuccess();

  public String verifyEmailResendFailure();

  public String verifyEmailChangePrompt();

  public String verifyEmailChangeSuccess();

  public String verifyEmailChangeFailure();

  public String verifyEmailAlreadyExists();

  // Preferences
  public String preferencesUserDataTitle();

  public String preferencesSubmit();

  public String preferencesEmailAlreadyExists();

  public String preferencesSubmitSuccess();

  // Recover Login
  public String recoverLoginUsernameOrEmail();

  public String recoverLoginCaptchaTitle();

  public String recoverLoginSubmit();

  public String recoverLoginSuccess();

  public String recoverLoginCaptchaFailed();

  public String recoverLoginNoSuchUser();

  // Reset password
  public String resetPassordUsername();

  public String resetPasswordToken();

  public String resetPasswordNewPassword();

  public String resetPasswordRepeatPassword();

  public String resetPasswordSubmit();

  public String resetPasswordInvalidToken();

  public String resetPasswordNoSuchUser();

  public String resetPasswordSuccess();

  // Select User Window

  public String selectUserWindowTitle();

  public String selectUserWindowSelect();

  public String selectUserWindowCancel();

  // Select Group Window

  public String selectGroupWindowTitle();

  public String selectGroupWindowSelect();

  public String selectGroupWindowCancel();

}
