/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;

import org.roda.core.data.v2.ip.StoragePath;

/**
 * Default implementation of the {@link Binary} interface.
 * <p>
 * <i> A binary resource which contains metadata and binary content payload.</i>
 * </p>
 * </p>
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * @see Binary
 */
public class DefaultBinary extends AbstractResource implements Binary {

  private static final long serialVersionUID = 2220776180390113576L;

  private transient ContentPayload content;
  private Long sizeInBytes;
  private boolean reference;
  private Map<String, String> contentDigest;

  public DefaultBinary(StoragePath storagePath, ContentPayload content, Long sizeInBytes, boolean reference,
    Map<String, String> contentDigest) {
    super(storagePath, false);
    this.content = content;
    this.sizeInBytes = sizeInBytes;
    this.reference = reference;
    this.contentDigest = contentDigest;
  }

  /**
   * @return the content
   */
  public ContentPayload getContent() {
    return content;
  }

  /**
   * @param content
   *          the content to set
   */
  public void setContent(ContentPayload content) {
    this.content = content;
  }

  /**
   * @return the sizeInBytes
   */
  public Long getSizeInBytes() {
    return sizeInBytes;
  }

  /**
   * @param sizeInBytes
   *          the sizeInBytes to set
   */
  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  /**
   * @return the reference
   */
  public boolean isReference() {
    return reference;
  }

  /**
   * @param reference
   *          the reference to set
   */
  public void setReference(boolean reference) {
    this.reference = reference;
  }

  /**
   * @return the contentDigest
   */
  public Map<String, String> getContentDigest() {
    return contentDigest;
  }

  /**
   * @param contentDigest
   *          the contentDigest to set
   */
  public void setContentDigest(Map<String, String> contentDigest) {
    this.contentDigest = contentDigest;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((content == null) ? 0 : content.hashCode());
    result = prime * result + ((contentDigest == null) ? 0 : contentDigest.hashCode());
    result = prime * result + (reference ? 1231 : 1237);
    result = prime * result + ((sizeInBytes == null) ? 0 : sizeInBytes.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DefaultBinary other = (DefaultBinary) obj;
    if (content == null) {
      if (other.content != null) {
        return false;
      }
    } else if (!content.equals(other.content)) {
      return false;
    }
    if (contentDigest == null) {
      if (other.contentDigest != null) {
        return false;
      }
    } else if (!contentDigest.equals(other.contentDigest)) {
      return false;
    }
    if (reference != other.reference) {
      return false;
    }
    if (sizeInBytes == null) {
      if (other.sizeInBytes != null) {
        return false;
      }
    } else if (!sizeInBytes.equals(other.sizeInBytes)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DefaultBinary [isDirectory()=" + isDirectory() + ", getStoragePath()=" + getStoragePath() + ", content="
      + content + ", sizeInBytes=" + sizeInBytes + ", reference=" + reference + ", contentDigest=" + contentDigest
      + "]";
  }

}
