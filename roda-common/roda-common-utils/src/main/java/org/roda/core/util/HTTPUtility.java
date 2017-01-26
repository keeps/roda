/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.Pair;

/**
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public final class HTTPUtility {
  public static final String METHOD_DELETE = "DELETE";
  public static final String METHOD_GET = "GET";

  /** Private empty constructor */
  private HTTPUtility() {

  }

  public static String doMethod(String url, String method, Optional<Pair<String, String>> basicAuth)
    throws GenericException {
    String res = null;
    try {
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod(method);
      addBasicAuthToConnection(con, basicAuth);
      int responseCode = con.getResponseCode();
      if (responseCode == 200) {
        InputStream is = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        is.close();
        res = response.toString();
      } else {
        throw new GenericException("Unable to connect to server, response code: " + responseCode);
      }
    } catch (IOException e) {
      throw new GenericException("Unable to connect to server", e);
    }
    return res;
  }

  public static String doGet(String url) throws GenericException {
    return doMethod(url, METHOD_GET, Optional.empty());
  }

  public static String doGet(String url, Optional<Pair<String, String>> basicAuth) throws GenericException {
    return doMethod(url, METHOD_GET, basicAuth);
  }

  public static String doDelete(String url) throws GenericException {
    return doMethod(url, METHOD_DELETE, Optional.empty());
  }

  public static String doDelete(String url, Optional<Pair<String, String>> basicAuth) throws GenericException {
    return doMethod(url, METHOD_DELETE, basicAuth);
  }

  private static void addBasicAuthToConnection(HttpURLConnection connection,
    Optional<Pair<String, String>> credentials) {
    if (credentials.isPresent()) {
      String encoded = new String(Base64.encode((credentials.get().getFirst() + ":" + credentials.get().getSecond())
        .getBytes(Charset.forName(RodaConstants.DEFAULT_ENCODING))));
      connection.setRequestProperty("Authorization", "Basic " + encoded);
    }
  }
}
