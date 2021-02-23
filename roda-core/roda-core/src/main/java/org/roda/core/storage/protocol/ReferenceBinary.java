package org.roda.core.storage.protocol;

import java.util.Map;

import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ReferenceBinary implements Binary {
  ContentPayload contentPayload;
  StoragePath storagePath;

  public ReferenceBinary(StoragePath storagePath, ContentPayload contentPayload) {
    this.contentPayload = contentPayload;
    this.storagePath = storagePath;
  }

  @Override
  public ContentPayload getContent() {
    return contentPayload;
  }

  @Override
  public Long getSizeInBytes() {
    return 0L;
  }

  @Override
  public boolean isReference() {
    return true;
  }

  @Override
  public Map<String, String> getContentDigest() {
    return null;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public StoragePath getStoragePath() {
    return storagePath;
  }
}
