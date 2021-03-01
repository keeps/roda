package org.roda.core.storage.protocol;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HTTPProtocolManager implements ProtocolManager {
  private final URI connectionString;

  public HTTPProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return connectionString.toURL().openStream();
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

  @Override
  public Long getSize() throws IOException {
    URLConnection conn = null;
    try {
      conn = connectionString.toURL().openConnection();
      if (conn instanceof HttpURLConnection) {
        ((HttpURLConnection) conn).setRequestMethod("HEAD");
      }
      conn.getInputStream();
      return conn.getContentLengthLong();
    } finally {
      if (conn instanceof HttpURLConnection) {
        ((HttpURLConnection) conn).disconnect();
      }
    }
  }

  @Override
  public void downloadResource(Path target) throws IOException {
    Path output = target.resolve(FilenameUtils.getName(connectionString.getPath()));
    ReadableByteChannel readableByteChannel = Channels.newChannel(getInputStream());
    FileOutputStream fileOutputStream = new FileOutputStream(output.toString());
    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
  }
}
