/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.common;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.roda.api.controllers.UserLogin;
import org.roda.common.RodaCoreFactory;
import org.roda.common.RodaCoreService;
import org.roda.common.ServiceException;
import org.roda.common.UserUtility;
import org.roda.core.common.AuthenticationDeniedException;
import org.roda.core.common.RODAException;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.RodaSimpleUser;
import org.roda.core.data.v2.RodaUser;
import org.roda.wui.client.common.UserLoginService;
import org.roda.wui.common.client.GenericException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * User login servlet
 * 
 * @author Luis Faria
 * 
 */
public class UserLoginServiceImpl extends RemoteServiceServlet implements UserLoginService {

  private static final long serialVersionUID = -6898933466651262033L;
  private static final String LOG_ACTION_WUI_LOGIN = "RODAWUI.login";
  private static Logger logger = Logger.getLogger(UserLoginServiceImpl.class);

  public static UserLoginServiceImpl getInstance() {
    return new UserLoginServiceImpl();
  }

  public RodaUser getAuthenticatedUser() throws RODAException {
    RodaUser user = UserUtility.getUser(this.getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    logger.debug("Serving user " + user + " from user " + user);
    return user;
  }

  public RodaUser login(String username, String password) throws AuthenticationDeniedException, GenericException {
    return UserLogin.login(username, password, this.getThreadLocalRequest());
  }

  public Map<String, String> getRodaProperties() {
    return RodaCoreFactory.getLoginRelatedProperties();

  }

}
