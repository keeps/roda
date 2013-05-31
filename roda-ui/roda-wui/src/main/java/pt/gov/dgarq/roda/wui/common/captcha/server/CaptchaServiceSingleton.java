/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.captcha.server;

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
