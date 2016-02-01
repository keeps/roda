package org.roda.wui.management.user.client.recaptcha;

public class Recaptcha {
  public static native void render(String key, String div, String lang) /*-{
    $wnd.grecaptcha.render(div, {
      'sitekey': key,
      'hl': lang,
      'theme' : 'light',
      'type': 'image'
    });
  }-*/;
  
  public static native void reset(String div) /*-{
    $wnd.grecaptcha.reset(div);
  }-*/;
  
  public static native void getResponse(String div) /*-{
    $wnd.grecaptcha.getResponse(div);
  }-*/;
}