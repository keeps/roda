/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.captcha.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public abstract class AbstractImageCaptcha {

  private static String getImageUrl() {
    return GWT.getModuleBaseURL() + "jcaptcha" + "?rand=" + System.currentTimeMillis();
  }

  private static final int TIMEOUT_DELAY = 180000;

  private final Image captchaImage;

  private final Timer timeout;

  /**
   * Abstract construtor to initialize image and timeout scheduler
   * 
   */
  public AbstractImageCaptcha() {
    captchaImage = new Image(getImageUrl());
    timeout = new Timer() {
      public void run() {
        onTimeout();
      }
    };
    timeout.schedule(TIMEOUT_DELAY);
  }

  protected void onTimeout() {
    refresh();
  }

  /**
   * Create the captcha panel
   * 
   * @return the panel widget
   * 
   */
  public abstract Widget getWidget();

  /**
   * Get the user answer input
   * 
   * @return the answer input
   */
  public abstract String getResponse();

  /**
   * Get the captcha image
   * 
   * @return the image
   */
  public Image getImage() {
    return captchaImage;
  }

  /**
   * Check the response against the server
   * 
   * @param callback
   *          Handle the response. Boolean.TRUE if passed, Boolean.FALSE
   *          otherwise
   */
  public void checkResponse(AsyncCallback<Boolean> callback) {
    CaptchaService.Util.getInstance().submit(getResponse(), callback);
  }

  /**
   * Refresh the captcha image
   * 
   */
  public void refresh() {
    captchaImage.setUrl(getImageUrl());
    timeout.cancel();
    timeout.schedule(TIMEOUT_DELAY);
  }

}
