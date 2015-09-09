/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.captcha.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface CaptchaServiceAsync {

  /**
   * Submit the captcha response to server
   * 
   * @param response
   *          the response to the captcha challenge
   * @return Boolean.TRUE if response is correct
   */
  public void submit(String response, AsyncCallback<Boolean> callback);

}
