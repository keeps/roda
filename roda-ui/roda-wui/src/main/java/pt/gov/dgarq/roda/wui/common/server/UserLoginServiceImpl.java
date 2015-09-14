package pt.gov.dgarq.roda.wui.common.server;

import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.common.ServiceException;
import org.roda.common.UserUtility;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.client.UserLoginService;

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
    // FIXME log action
    try {
      RodaUser user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      UserUtility.setUser(this.getThreadLocalRequest(),
        new RodaSimpleUser(user.getId(), user.getName(), user.getEmail(), user.isGuest()));
      return user;
    } catch (ServiceException e) {
      throw new GenericException(e.getMessage());
    }
  }

  public Map<String, String> getRodaProperties() {
    return RodaCoreFactory.getLoginRelatedProperties();

  }

}
