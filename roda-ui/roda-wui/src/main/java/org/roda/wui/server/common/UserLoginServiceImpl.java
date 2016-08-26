/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.api.controllers.UserLogin;
import org.roda.wui.client.common.UserLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * User login servlet
 * 
 * @author Luis Faria
 * 
 */
public class UserLoginServiceImpl extends RemoteServiceServlet implements UserLoginService {

  private static final long serialVersionUID = -6898933466651262033L;
  @SuppressWarnings("unused")
  private static final String LOG_ACTION_WUI_LOGIN = "RODAWUI.login";
  private static final String WUI_LOGIN_CACHE = "WUI_LOGIN_CACHE";
  private static final List<String> WUI_LOGIN_CACHE_PREFIXES = Arrays.asList("ui.menu.", "ui.role.");
  private static Logger logger = LoggerFactory.getLogger(UserLoginServiceImpl.class);

  public static UserLoginServiceImpl getInstance() {
    return new UserLoginServiceImpl();
  }

  public RodaSimpleUser getAuthenticatedUser() throws RODAException {
    RodaSimpleUser user = UserUtility.getUser(this.getThreadLocalRequest());
    logger.debug("Serving user {}", user);
    return user;
  }

  public RodaSimpleUser login(String username, String password) throws AuthenticationDeniedException, GenericException {
    RodaSimpleUser user = UserLogin.login(username, password, this.getThreadLocalRequest());
    logger.debug("Logged user {}", user);
    return user;
  }

  public Map<String, String> getRodaProperties() {
    return RodaCoreFactory.getPropertiesFromCache(WUI_LOGIN_CACHE, WUI_LOGIN_CACHE_PREFIXES);

  }

}
