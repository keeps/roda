/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.management;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.data.utils.JsonUtils;
import org.roda.wui.client.management.recaptcha.RecaptchaException;

import com.fasterxml.jackson.databind.JsonNode;

public class RecaptchaUtils {
  private RecaptchaUtils() {
    // do nothing
  }

  public static void recaptchaVerify(String secret, String captcha) throws RecaptchaException {
    BufferedReader bufferedReader = null;
    try {
      String urlParameters = "secret=" + secret + "&response=" + captcha;
      String url = "https://www.google.com/recaptcha/api/siteverify?" + urlParameters;
      String userAgent = "Mozilla/5.0";

      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);

      request.addHeader("User-Agent", userAgent);
      HttpResponse response = client.execute(request);

      StringBuilder builder = new StringBuilder();
      bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

      for (String line = null; (line = bufferedReader.readLine()) != null;) {
        builder.append(line).append("\n");
      }
      JsonNode jsonObject = JsonUtils.parseJson(builder.toString());
      boolean success = jsonObject.get("success").asBoolean(false);
      if (!success) {
        throw new RecaptchaException("ReCAPTCHA verification failed");
      }
    } catch (Exception e) {
      throw new RecaptchaException("ReCAPTCHA verification failed", e);
    } finally {
      IOUtils.closeQuietly(bufferedReader);
    }
  }
}
