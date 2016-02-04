package org.roda.wui.server.management;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.roda.wui.client.management.recaptcha.RecaptchaException;

public class RecaptchaUtils {
  public static void recaptchaVerify(String secret, String captcha) throws RecaptchaException {
    try {

      String urlParameters = "secret=" + secret + "&response=" + captcha;
      String url = "https://www.google.com/recaptcha/api/siteverify?" + urlParameters;
      String USER_AGENT = "Mozilla/5.0";

      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);

      request.addHeader("User-Agent", USER_AGENT);
      HttpResponse response = client.execute(request);

      StringBuilder builder = new StringBuilder();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

      for (String line = null; (line = bufferedReader.readLine()) != null;) {
        builder.append(line).append("\n");
      }
      JSONObject jsonObject = new JSONObject(builder.toString());
      boolean success = jsonObject.getBoolean("success");
      if (!success) {
        throw new RecaptchaException("ReCAPTCHA verification failed");
      }
    } catch (Exception e) {
      throw new RecaptchaException("ReCAPTCHA verification failed", e);
    }
  }
}
