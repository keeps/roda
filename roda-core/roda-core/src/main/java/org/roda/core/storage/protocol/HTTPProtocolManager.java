package org.roda.core.storage.protocol;

import org.roda.core.storage.DefaultBinary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HTTPProtocolManager implements ProtocolManager{
  URI connectionString;

  public HTTPProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
  }

  @Override
  public InputStream getInputStream() {
    try {
      return connectionString.toURL().openStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Boolean isAvailable() {
    try {
      URL url = connectionString.toURL();
      HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      huc.setRequestMethod("HEAD");

      int responseCode = huc.getResponseCode();

      return HttpURLConnection.HTTP_OK == responseCode;
    } catch (IOException e) {
      return false;
    }
  }
}
