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
package org.roda.wui.common.captcha.client;

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
