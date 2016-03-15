package org.roda.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.roda.core.data.exceptions.GenericException;

public class HTTPUtility {
  public static String doGet(String url) throws IOException, GenericException {
    String res = null;
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("GET");
    int responseCode = con.getResponseCode();
    if (responseCode == 200) {
      InputStream is = con.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      is.close();
      res = response.toString();
    } else {
      throw new GenericException("Unable to connect to siegfried server");
    }
    return res;
  }
}
