/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import java.io.Serializable;

import org.roda.core.common.EntityResponse;

public class ObjectResponse<T extends Serializable> implements EntityResponse {
  private String mediaType;
  private T object;

  public ObjectResponse(String mediaType, T object) {
    super();
    this.mediaType = mediaType;
    this.object = object;
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }

  @Override
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }

}
