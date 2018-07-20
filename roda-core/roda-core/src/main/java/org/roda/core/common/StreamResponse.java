/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.Date;

public class StreamResponse implements EntityResponse {
  private String filename;
  private String mediaType;
  private long fileSize = -1;
  private Date lastModified;
  private ConsumesOutputStream stream;

  public StreamResponse(ConsumesOutputStream stream) {
    super();
    this.filename = stream.getFileName();
    this.fileSize = stream.getSize();
    this.mediaType = stream.getMediaType();
    this.lastModified = stream.getLastModified();
    this.stream = stream;
  }

  public StreamResponse(ConsumesOutputStream stream, long fileSize) {
    this(stream);
    this.fileSize = fileSize;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }

  @Override
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public ConsumesOutputStream getStream() {
    return stream;
  }

  public void setStream(ConsumesOutputStream stream) {
    this.stream = stream;
  }

  /**
   * @return the lastModified
   */
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * @param lastModified
   *          the lastModified to set
   */
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

}
