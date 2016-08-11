/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import javax.ws.rs.core.StreamingOutput;

public class StreamResponse implements EntityResponse {
  private String filename;
  private String mediaType;
  private StreamingOutput stream;

  public StreamResponse(String filename, String mediaType, StreamingOutput stream) {
    super();
    this.filename = filename;
    this.mediaType = mediaType;
    this.stream = stream;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public StreamingOutput getStream() {
    return stream;
  }

  public void setStream(StreamingOutput stream) {
    this.stream = stream;
  }

}
