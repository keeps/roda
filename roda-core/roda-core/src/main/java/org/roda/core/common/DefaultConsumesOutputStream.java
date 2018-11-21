/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.function.Consumer;

public class DefaultConsumesOutputStream implements ConsumesOutputStream {

  private final String fileName;
  private final String mediaType;
  private final Date lastModified;
  private final long sizeInBytes;
  private final Consumer<OutputStream> consumer;

  public DefaultConsumesOutputStream(String fileName, String mediaType, Date lastModified, long sizeInBytes,
    Consumer<OutputStream> consumer) {
    super();
    this.fileName = fileName;
    this.mediaType = mediaType;
    this.lastModified = lastModified;
    this.sizeInBytes = sizeInBytes;
    this.consumer = consumer;
  }

  public DefaultConsumesOutputStream(String fileName, String mediaType, Consumer<OutputStream> consumer) {
    this(fileName, mediaType, null, -1, consumer);
  }
  

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    consumer.accept(out);
  }

  @Override
  public Date getLastModified() {
    return this.lastModified;
  }

  @Override
  public String getFileName() {
    return this.fileName;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }

  @Override
  public long getSize() {
    return sizeInBytes;
  }

}
