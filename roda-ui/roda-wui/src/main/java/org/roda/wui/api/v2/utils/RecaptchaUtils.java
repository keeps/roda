/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.utils;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.roda.core.data.utils.JsonUtils;
import org.roda.wui.client.management.recaptcha.RecaptchaException;

import tools.jackson.databind.JsonNode;

public class RecaptchaUtils {
  private RecaptchaUtils() {
    // do nothing
  }

  public static void recaptchaVerify(String secret, String captcha) throws RecaptchaException {
    try {
      String urlParameters = "secret=" + secret + "&response=" + captcha;
      String url = "https://www.google.com/recaptcha/api/siteverify?" + urlParameters;
      String userAgent = "Mozilla/5.0";

      try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        // BasicHttpClientResponseHandler automatically handles resource deallocation,
        // checks for HTTP 2xx success status codes, and returns the body as a String.
        HttpClientResponseHandler<String> responseHandler = new BasicHttpClientResponseHandler();
        String jsonResponse = client.execute(request, responseHandler);

        JsonNode jsonObject = JsonUtils.parseJson(jsonResponse);
        boolean success = jsonObject.get("success").asBoolean(false);
        if (!success) {
          throw new RecaptchaException("ReCAPTCHA verification failed");
        }
      }
    } catch (Exception e) {
      throw new RecaptchaException("ReCAPTCHA verification failed", e);
    }
  }
}
