package org.roda.wui.common.captcha.server;

import org.apache.log4j.Logger;
import org.roda.wui.common.captcha.client.CaptchaService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.octo.captcha.service.CaptchaServiceException;

/**
 * Capcha service implemented with JCaptcha
 * 
 * @author Luis Faria
 */
public class CaptchaServiceImpl extends RemoteServiceServlet implements CaptchaService {

  /**
	 * 
	 */
  private static final long serialVersionUID = -7951555053175001506L;

  private static final Logger logger = Logger.getLogger(CaptchaServiceImpl.class);

  /**
   * Check is response to capcha chalenge is correct
   * 
   * @param sessionID
   *          the request session ID
   * @param response
   *          the user response to the chalenge
   * @return Boolean.TRUE if response is correct
   */
  public static Boolean check(String sessionID, String response) {
    Boolean isResponseCorrect = Boolean.FALSE;
    try {
      isResponseCorrect = CaptchaServiceSingleton.getInstance().validateResponseForID(sessionID, response);
    } catch (CaptchaServiceException e) {
      // should not happen, may be thrown if the id is not valid
      logger.error("Error checking capthca", e);
    }
    return isResponseCorrect;
  }

  public Boolean submit(String response) {
    return check(this.getThreadLocalRequest().getSession().getId(), response);
  }

}
