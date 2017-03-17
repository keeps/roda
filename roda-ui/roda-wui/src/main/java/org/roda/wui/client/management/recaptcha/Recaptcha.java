/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.recaptcha;

public class Recaptcha {

  private Recaptcha() {
    // do nothing
  }

  public static native void render(String key, String div, String lang) /*-{
		$wnd.grecaptcha.render(div, {
			'sitekey' : key,
			'hl' : lang,
			'theme' : 'light',
			'type' : 'image'
		});
  }-*/;

  public static native void reset() /*-{
		$wnd.grecaptcha.reset();
  }-*/;

  public static native String getResponse() /*-{
		return $wnd.grecaptcha.getResponse();
  }-*/;
}