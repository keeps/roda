package org.roda.core.protocols.protocols;

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
import org.roda.core.protocols.AbstractProtocol;
import org.roda.core.protocols.Protocol;
import org.roda.core.protocols.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HttpProtocol extends AbstractProtocol {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileProtocol.class);
  private static final String SCHEMA = "http";
  private static final String NAME = "Http protocol";
  private static final String VERSION = "0";

  public HttpProtocol(){
    super();
  }

  public HttpProtocol(URI uri) {
    setConnectionString(uri);
  }

  @Override
  public void init() throws ProtocolException {

  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Protocol cloneMe(URI uri) {
    return new HttpProtocol(uri);
  }

  @Override
  public String getSchema() {
    return SCHEMA;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return getConnectionString().toURL().openStream();
  }

  @Override
  public Boolean isAvailable() {
    try {
      URL url = getConnectionString().toURL();
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
      conn = getConnectionString().toURL().openConnection();
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
    Path output = target.resolve(FilenameUtils.getName(getConnectionString().getPath()));
    ReadableByteChannel readableByteChannel = Channels.newChannel(getInputStream());
    FileOutputStream fileOutputStream = new FileOutputStream(output.toString());
    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
  }

  @Override
  public void shutdown() {

  }

  @Override
  public String getId() {
    return null;
  }
}
