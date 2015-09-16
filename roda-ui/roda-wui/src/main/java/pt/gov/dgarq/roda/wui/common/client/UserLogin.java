/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.tools.CachedAsynRequest;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

/**
 * @author Luis Faria
 * 
 * 
 * 
 */
public class UserLogin {

  private static final ClientLogger logger = new ClientLogger(UserLogin.class.getName());

  // private static final CommonConstants constants = (CommonConstants)
  // GWT.create(CommonConstants.class);
  //
  // private static final CommonMessages messages = (CommonMessages)
  // GWT.create(CommonMessages.class);

  private static UserLoginServiceAsync userLoginService;

  private static UserLogin instance = null;

  private static Map<String, String> rodaProperties;

  private static boolean initialized = false;

  private static void init(final AsyncCallback<Map<String, String>> callback) {
    if (!initialized) {

      userLoginService = UserLoginService.Util.getInstance();
      userLoginService.getRodaProperties(new AsyncCallback<Map<String, String>>() {

        public void onFailure(Throwable caught) {
          logger.fatal("Error getting role mapping", caught);
          callback.onFailure(caught);
        }

        public void onSuccess(Map<String, String> properties) {
          rodaProperties = properties;
          initialized = true;
          callback.onSuccess(properties);
        }

      });
    } else {
      callback.onSuccess(rodaProperties);
    }
  }

  static {
    init(new AsyncCallback<Map<String, String>>() {

      public void onFailure(Throwable caught) {
        logger.error("Error initializing", caught);
      }

      public void onSuccess(Map<String, String> properties) {
        // nothing to do
      }

    });
  }

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static UserLogin getInstance() {
    if (instance == null) {
      instance = new UserLogin();
    }
    return instance;
  }

  private final List<LoginStatusListener> listeners;

  // private AuthenticatedUser user = null;

  private UserLogin() {
    listeners = new Vector<LoginStatusListener>();
  }

  private final CachedAsynRequest<RodaUser> getUserRequest = new CachedAsynRequest<RodaUser>() {

    @Override
    public void getFromServer(AsyncCallback<RodaUser> callback) {
      userLoginService.getAuthenticatedUser(callback);
    }
  };

  /**
   * Get current authenticated user. User is cached and only refreshed when
   * login or logout actions are called.
   * 
   * @param callback
   *          call back handler that receives error if failed or AuthOfficeUser
   *          if success.
   */
  public void getAuthenticatedUser(final AsyncCallback<RodaUser> callback) {
    getUserRequest.request(callback);
  }

  /**
   * Login into RODA Core
   * 
   * @param username
   * @param password
   * @param callback
   */
  public void login() {
    String currentURL = Window.Location.getHref().replaceAll("#", "%23");
    Window.open("/login?service=" + currentURL, "_self", "");
  }

  public void login(String username, String password, final AsyncCallback<RodaUser> callback) {
    userLoginService.login(username, password, new AsyncCallback<RodaUser>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(RodaUser newUser) {
        getUserRequest.setCached(newUser);
        onLoginStatusChanged(newUser);
        callback.onSuccess(newUser);
      }
    });
  }

  /**
   * 
   * @param callback
   */
  public void logout() {
    String currentURL = Window.Location.getHref().replaceAll("#", "%23");
    Window.open("/logout?service=" + currentURL, "_self", "");
    getUserRequest.clearCache();
  }

  /**
   * Add a login status listener
   * 
   * @param listener
   */
  public void addLoginStatusListener(LoginStatusListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a login status listener
   * 
   * @param listener
   */
  public void removeLoginStatusListener(LoginStatusListener listener) {
    listeners.remove(listener);
  }

  public void onLoginStatusChanged(RodaUser newUser) {
    for (LoginStatusListener listener : listeners) {
      listener.onLoginStatusChanged(newUser);
    }
  }

  /**
   * Check if the changing of the permissions of the RODA member affects current
   * authenticated user and alert login status listeners
   * 
   * @param member
   *          the member which had his permissions changed
   */
  public void permissionsChanged(final RODAMember member) {
    getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        // do nothing
      }

      public void onSuccess(RodaUser user) {
        RodaUser authUser = user;
        if (member instanceof User && member.getName().equals(authUser.getName())) {
          onLoginStatusChanged(authUser);
        } else if (member instanceof Group && Arrays.asList(authUser.getAllGroups()).contains(member.getName())) {
          onLoginStatusChanged(authUser);
        }
      }

    });
  }

  /**
   * If any evidence is found that the login was reset, call this method to
   * update login and alert all listeners
   */
  // public void checkForLoginReset() {
  // final User currentUser = user;
  // user = null;
  // getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {
  //
  // public void onFailure(Throwable caught) {
  // logger.error("Error getting current authenticated user", caught);
  // }
  //
  // public void onSuccess(AuthenticatedUser user) {
  // if (!user.equals(currentUser)) {
  // onLoginStatusChanged(user);
  // }
  //
  // }
  //
  // });
  // }

  /**
   * Get RODA properties
   * 
   * @param callback
   */
  public static void getRodaProperties(final AsyncCallback<Map<String, String>> callback) {
    init(new AsyncCallback<Map<String, String>>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(Map<String, String> properties) {
        callback.onSuccess(properties);

      }
    });
  }

  /**
   * Get a RODA property
   * 
   * @param key
   * @param callback
   */
  public static void getRodaProperty(final String key, final AsyncCallback<String> callback) {
    init(new AsyncCallback<Map<String, String>>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(Map<String, String> properties) {
        callback.onSuccess(properties.get(key));
      }

    });
  }

  /**
   * Check if current user has permission to access a history resolver
   * 
   * @param res
   * @param callback
   */
  public void checkRole(final HistoryResolver res, final AsyncCallback<Boolean> callback) {
    String historyKey = Tools.join(res.getHistoryPath(), ".");
    String propertyName = "menu." + historyKey + ".role";
    UserLogin.getRodaProperty(propertyName, new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(final String role) {
        if (role == null) {
          GWT.log("Could not find role for path " + res.getHistoryPath());
        }
        getAuthenticatedUser(new AsyncCallback<RodaUser>() {

          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          public void onSuccess(RodaUser authUser) {
            callback.onSuccess(new Boolean(authUser.hasRole(role)));
          }

        });

      }

    });
  }

  /**
   * Check if current authenticated user can access some history resolvers
   * 
   * @param res
   * @param exclusive
   * @param callback
   */
  public void checkRoles(HistoryResolver[] res, final boolean exclusive, final AsyncCallback<Boolean> callback) {
    final Boolean[] results = new Boolean[res.length];

    for (int i = 0; i < res.length; i++) {
      final int index = i;
      checkRole(res[i], new AsyncCallback<Boolean>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(Boolean rolePermitted) {
          results[index] = rolePermitted;
          boolean lastOne = true;
          for (int i = 0; i < results.length; i++) {
            if (results[i] == null) {
              lastOne = false;
            }
          }
          if (lastOne) {
            if (exclusive) {
              boolean ret = true;
              for (int i = 0; i < results.length; i++) {
                ret = results[i].booleanValue() ? ret : false;
              }
              callback.onSuccess(new Boolean(ret));
            } else {
              boolean ret = false;
              for (int i = 0; i < results.length; i++) {
                ret = results[i].booleanValue() ? true : ret;
              }
              callback.onSuccess(new Boolean(ret));
            }
          }
        }

      });
    }
  }

}
