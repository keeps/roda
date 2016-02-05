package org.roda.wui.client.management.recaptcha;

public class Recaptcha {
  public static native void render(String key, String div, String lang) /*-{
    $wnd.grecaptcha.render(div, {
      'sitekey': key,
      'hl': lang,
      'theme' : 'light',
      'type': 'image'
    });
  }-*/;

  public static native void reset() /*-{
    $wnd.grecaptcha.reset();
  }-*/;

  public static native String getResponse() /*-{
    return $wnd.grecaptcha.getResponse();
  }-*/;
}