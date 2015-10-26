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
package org.roda.wui.common.captcha.server;

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * @see <a
 *      href="http://forge.octo.com/jcaptcha/confluence/display/general/5+minutes+application+integration+tutorial?showComments=false">JCaptcha
 *      quick integration tutorial</a>
 */
public class CaptchaServiceSingleton {
  private static ImageCaptchaService instance = new DefaultManageableImageCaptchaService();

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static ImageCaptchaService getInstance() {
    return instance;
  }
}
