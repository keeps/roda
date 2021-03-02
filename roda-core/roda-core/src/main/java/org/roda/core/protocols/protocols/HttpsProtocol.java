package org.roda.core.protocols.protocols;

import org.roda.core.protocols.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HttpsProtocol extends HttpProtocol {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileProtocol.class);
  private static final String SCHEMA = "https";
  private static final String NAME = "Https protocol";
  private static final String VERSION = "0";

  public HttpsProtocol() {
    super();
  }
  public HttpsProtocol(URI uri) {
    super(uri);
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
    return new HttpsProtocol(uri);
  }

  @Override
  public String getSchema() {
    return SCHEMA;
  }
}
