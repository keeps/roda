/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

public class IndexedRepresentation extends Representation {

  private static final long serialVersionUID = -950545608880793468L;

  private long sizeInBytes;
  private long totalNumberOfFiles;

  public IndexedRepresentation() {
    super();
  }

  public IndexedRepresentation(String id, String aipId, boolean original, long sizeInBytes, long totalNumberOfFiles) {
    super(id, aipId, original);
    this.sizeInBytes = sizeInBytes;
    this.totalNumberOfFiles = totalNumberOfFiles;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  public long getTotalNumberOfFiles() {
    return totalNumberOfFiles;
  }

  public void setTotalNumberOfFiles(long totalNumberOfFiles) {
    this.totalNumberOfFiles = totalNumberOfFiles;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
    result = prime * result + (int) (totalNumberOfFiles ^ (totalNumberOfFiles >>> 32));
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
    if (totalNumberOfFiles != other.totalNumberOfFiles)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedRepresentation [sizeInBytes=" + sizeInBytes + ", totalNumberOfFiles=" + totalNumberOfFiles
      + ", super.toString()=" + super.toString() + "]";
  }

}
