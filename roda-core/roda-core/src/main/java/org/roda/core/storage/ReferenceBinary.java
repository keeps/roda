package org.roda.core.storage;

import java.util.Map;

import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ReferenceBinary implements Binary {
  private ContentPayload contentPayload;
  private StoragePath storagePath;
  private Long sizeInBytes;

  public ReferenceBinary(StoragePath storagePath, ContentPayload contentPayload, Long sizeInBytes) {
    this.contentPayload = contentPayload;
    this.storagePath = storagePath;
    this.sizeInBytes = sizeInBytes;
  }

  @Override
  public ContentPayload getContent() {
    return contentPayload;
  }

  @Override
  public Long getSizeInBytes() {
    return sizeInBytes;
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
