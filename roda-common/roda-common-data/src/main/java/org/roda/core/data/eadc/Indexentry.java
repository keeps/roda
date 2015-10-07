package org.roda.core.data.eadc;

import java.io.Serializable;

public class Indexentry implements Serializable {

  private static final long serialVersionUID = 8532313887889843538L;

  private String subject;

  public Indexentry() {
  }

  public Indexentry(String subject) {
    this.subject = subject;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject
   *          the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public String toString() {
    return "Indexentry [subject=" + subject + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    Indexentry other = (Indexentry) obj;
    if (subject == null) {
      if (other.subject != null) {
        return false;
      }
    } else if (!subject.equals(other.subject)) {
      return false;
    }
    return true;
  }
}
