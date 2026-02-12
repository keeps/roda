/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.roda.core.data.v2.ConsumesSkipableOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeConsumesOutputStream implements ConsumesSkipableOutputStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(RangeConsumesOutputStream.class);

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private final SeekableContentPayload payload;
  private final String filename;
  private final String mediaType;
  private final Date lastModified;
  private final long size;

  public RangeConsumesOutputStream(SeekableContentPayload payload, String filename, Date lastModified, long size,
      String mediaType) {
    this.payload = payload;
    this.filename = filename;
    this.lastModified = lastModified;
    this.size = size;
    this.mediaType = mediaType;
  }

  public RangeConsumesOutputStream(SeekableContentPayload payload, Binary binary) {
    this(payload, binary, DEFAULT_MIME_TYPE);
  }

  public RangeConsumesOutputStream(SeekableContentPayload payload, Binary binary, String mediaType) {
    this.payload = payload;
    this.filename = binary.getStoragePath().getName();
    this.lastModified = new Date(); // TODO missing information about binary last modified date
    this.size = binary.getSizeInBytes();
    this.mediaType = mediaType;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    payload.writeTo(out, 0, getSize());
  }

  @Override
  public void consumeOutputStream(OutputStream out, int from, int len) throws IOException {
    payload.writeTo(out, from, len);
  }

  @Override
  public void consumeOutputStream(OutputStream out, long from, long end) {
    try {
      payload.writeTo(out, from, end - from + 1);
    } catch (IOException e) {
      LOGGER.warn("Error writing to output stream", e);
    }
  }

  @Override
  public Date getLastModified() {
    return lastModified;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }
}
