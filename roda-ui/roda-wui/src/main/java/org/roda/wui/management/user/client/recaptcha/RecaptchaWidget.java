package org.roda.wui.management.user.client.recaptcha;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class RecaptchaWidget extends Composite {
  protected static final String RECAPTCHA_DIV_HTML = "<div id=\"recaptcha_div\"/>";
  protected static final String RECAPTCHA_DIV_ID = "recaptcha_div";

  private HTML html;
  private String key;

  /**
   * This Constructor is used to create an default ReCAPTCHA widget
   * 
   * @param key
   *          Your public key
   */
  public RecaptchaWidget(String key) {
    this.key = key;
    html = new HTML(RECAPTCHA_DIV_HTML);
    initWidget(html);
  }

  @Override
  protected void onAttach() {
    render();
    super.onAttach();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
  }

  /**
   * This Method is used to create an default ReCAPTCHA widget
   */
  protected void render() {
    Recaptcha.render(key, RECAPTCHA_DIV_ID, "en");
  }

  protected void reset() {
    Recaptcha.reset(RECAPTCHA_DIV_ID);
  }
  
  protected void getResponse() {
    Recaptcha.getResponse(RECAPTCHA_DIV_ID);
  }
}