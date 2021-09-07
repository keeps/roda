package org.roda.core.protocols.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.roda.core.data.utils.URLUtils;
import org.roda.core.protocols.AbstractProtocol;
import org.roda.core.protocols.Protocol;
import org.roda.core.protocols.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class RODAProtocol extends AbstractProtocol {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODAProtocol.class);
  private static final String SCHEMA = "roda";
  private static final String NAME = "RODA protocol";
  private static final String VERSION = "0";
  private Path path;

  public RODAProtocol() {
    super();
  }

  public RODAProtocol(URI uri) {
    setConnectionString(uri);
    this.path = Paths.get(URLUtils.decode(uri.getPath()));
  }

  @Override
  public void init() throws ProtocolException {
    LOGGER.info("init");
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
    return new RODAProtocol(uri);
  }

  @Override
  public String getSchema() {
    return SCHEMA;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public Boolean isAvailable() {
    return Files.exists(path);
  }

  @Override
  public Long getSize() throws IOException {
    return Files.size(path);
  }

  @Override
  public void downloadResource(Path target) throws IOException {
    /* do nothing */
  }

  @Override
  public void shutdown() {
    /* do nothing */
  }

  @Override
  public String getId() {
    return null;
  }
}
