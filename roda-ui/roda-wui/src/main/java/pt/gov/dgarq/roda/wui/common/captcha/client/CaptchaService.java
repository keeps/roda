/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.captcha.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface CaptchaService extends RemoteService {

	/**
	 * Service URI
	 */
	public static final String SERVICE_URI = "/captchaservice";

	/**
	 * Implementation accessor
	 * 
	 */
	public static class Util {

		/**
		 * Get implementation async instance
		 * 
		 * @return the instance
		 */
		public static CaptchaServiceAsync getInstance() {

			CaptchaServiceAsync instance = (CaptchaServiceAsync) GWT
					.create(CaptchaService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}

	/**
	 * Submit the captcha response to server
	 * 
	 * @param response
	 *            the response to the captcha challenge
	 * @return Boolean.TRUE if response is correct
	 */
	public Boolean submit(String response);

}
