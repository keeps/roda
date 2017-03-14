/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class Fixity implements Serializable {
  private static final long serialVersionUID = 5643738632324867731L;

  private String messageDigestAlgorithm = null;
  private String messageDigest = null;
  private String messageDigestOriginator = null;

  /**
   * Constructs a new {@link Fixity}.
   */
  public Fixity() {
    // do nothing
  }

  /**
   * Constructs a new {@link Fixity} cloning an existing {@link Fixity}.
   * 
   * @param fixity
   */
  public Fixity(Fixity fixity) {
    this(fixity.getMessageDigest(), fixity.getMessageDigestAlgorithm(), fixity.getMessageDigestOriginator());
  }

  /**
   * Constructs a new {@link Fixity} with the given parameters.
   * 
   * @param messageDigestAlgorithm
   * @param messageDigest
   * @param messageDigestOriginator
   * 
   */
  public Fixity(String messageDigestAlgorithm, String messageDigest, String messageDigestOriginator) {
    setMessageDigestAlgorithm(messageDigestAlgorithm);
    setMessageDigest(messageDigest);
    setMessageDigestOriginator(messageDigestOriginator);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((messageDigest == null) ? 0 : messageDigest.hashCode());
    result = prime * result + ((messageDigestAlgorithm == null) ? 0 : messageDigestAlgorithm.hashCode());
    result = prime * result + ((messageDigestOriginator == null) ? 0 : messageDigestOriginator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Fixity)) {
      return false;
    }
    Fixity other = (Fixity) obj;
    if (messageDigest == null) {
      if (other.messageDigest != null) {
        return false;
      }
    } else if (!messageDigest.equals(other.messageDigest)) {
      return false;
    }
    if (messageDigestAlgorithm == null) {
      if (other.messageDigestAlgorithm != null) {
        return false;
      }
    } else if (!messageDigestAlgorithm.equals(other.messageDigestAlgorithm)) {
      return false;
    }
    if (messageDigestOriginator == null) {
      if (other.messageDigestOriginator != null) {
        return false;
      }
    } else if (!messageDigestOriginator.equals(other.messageDigestOriginator)) {
      return false;
    }
    return true;
  }

  /**
   * @return a {@link String} with this object's info.
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "Fixity(messageDigestAlgorithm=" + getMessageDigestAlgorithm() + ", messageDigest" + getMessageDigest()
      + ", messageDigestOriginator" + getMessageDigestOriginator() + ")";
  }

  /**
   * @return the messageDigestAlgorithm
   */
  public String getMessageDigestAlgorithm() {
    return messageDigestAlgorithm;
  }

  /**
   * @param messageDigestAlgorithm
   *          the messageDigestAlgorithm to set
   */
  public void setMessageDigestAlgorithm(String messageDigestAlgorithm) {
    this.messageDigestAlgorithm = messageDigestAlgorithm;
  }

  /**
   * @return the messageDigest
   */
  public String getMessageDigest() {
    return messageDigest;
  }

  /**
   * @param messageDigest
   *          the messageDigest to set
   */
  public void setMessageDigest(String messageDigest) {
    this.messageDigest = messageDigest;
  }

  /**
   * @return the messageDigestOriginator
   */
  public String getMessageDigestOriginator() {
    return messageDigestOriginator;
  }

  /**
   * @param messageDigestOriginator
   *          the messageDigestOriginator to set
   */
  public void setMessageDigestOriginator(String messageDigestOriginator) {
    this.messageDigestOriginator = messageDigestOriginator;
  }
}
