package org.roda.core.protocols.protocols;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.data.utils.URLUtils;
import org.roda.core.protocols.AbstractProtocol;
import org.roda.core.protocols.Protocol;
import org.roda.core.protocols.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FileProtocol extends AbstractProtocol {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileProtocol.class);
  private static final String SCHEMA = "file";
  private static final String NAME = "File protocol";
  private static final String VERSION = "0";
  private Path path;

  public FileProtocol() {
    super();
  }

  public FileProtocol(URI uri) {
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
    return new FileProtocol(uri);
  }

  @Override
  public String getSchema() {
    return SCHEMA;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(path.toFile());
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
    Path output = target.resolve(FilenameUtils.getName(path.toString()));
    Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public void shutdown() {

  }

  @Override
  public String getId() {
    return null;
  }
}
