package org.roda.core.data.v2.ip;

import java.util.List;

public class IndexedRepresentation extends Representation {

  private static final long serialVersionUID = -950545608880793468L;

  private long sizeInBytes;

  public IndexedRepresentation() {
    super();
  }

  public IndexedRepresentation(String id, String aipId, boolean original, List<String> directFileIds,
    long sizeInBytes) {
    super(id, aipId, original, directFileIds);
    this.sizeInBytes = sizeInBytes;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedRepresentation other = (IndexedRepresentation) obj;
    if (sizeInBytes != other.sizeInBytes)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedRepresentation [sizeInBytes=" + sizeInBytes + ", getId()=" + getId() + ", getAipId()=" + getAipId()
      + ", isOriginal()=" + isOriginal() + ", getFilesDirectlyUnder()=" + getFilesDirectlyUnder() + "]";
  }

}
