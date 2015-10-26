/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class ControlAccess implements Serializable {

  private static final long serialVersionUID = -3124085158684938640L;

  private String attributeEncodinganalog = null;
  private String attributeSource = null;
  private String head = null;
  private String subject = null;
  private String function = null;
  private String p = null;

  /**
   * Constructs a new empty {@link ControlAccess}
   * */
  public ControlAccess() {

  }

  /**
   * Constructs a new {@link ControlAccess} using provided parameters
   * 
   * @param attributeEncodinganalog
   * @param attributeSource
   * @param head
   * @param subject
   * @param function
   * @param p
   */
  public ControlAccess(String attributeEncodinganalog, String attributeSource, String head, String subject,
    String function, String p) {
    this.attributeEncodinganalog = attributeEncodinganalog;
    this.attributeSource = attributeSource;
    this.head = head;
    this.subject = subject;
    this.function = function;
    this.p = p;
  }

  public String getAttributeEncodinganalog() {
    return attributeEncodinganalog;
  }

  public void setAttributeEncodinganalog(String attributeEncodinganalog) {
    this.attributeEncodinganalog = attributeEncodinganalog;
  }

  public String getAttributeSource() {
    return attributeSource;
  }

  public void setAttributeSource(String attributeSource) {
    this.attributeSource = attributeSource;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public String getP() {
    return p;
  }

  public void setP(String p) {
    this.p = p;
  }

  @Override
  public String toString() {
    return "ControlAccess [attributeEncodinganalog=" + attributeEncodinganalog + ", attributeSource=" + attributeSource
      + ", head=" + head + ", subject=" + subject + ", function=" + function + ", p=" + p + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeEncodinganalog == null) ? 0 : attributeEncodinganalog.hashCode());
    result = prime * result + ((attributeSource == null) ? 0 : attributeSource.hashCode());
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    result = prime * result + ((head == null) ? 0 : head.hashCode());
    result = prime * result + ((p == null) ? 0 : p.hashCode());
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
    ControlAccess other = (ControlAccess) obj;
    if (attributeEncodinganalog == null) {
      if (other.attributeEncodinganalog != null) {
        return false;
      }
    } else if (!attributeEncodinganalog.equals(other.attributeEncodinganalog)) {
      return false;
    }
    if (attributeSource == null) {
      if (other.attributeSource != null) {
        return false;
      }
    } else if (!attributeSource.equals(other.attributeSource)) {
      return false;
    }
    if (function == null) {
      if (other.function != null) {
        return false;
      }
    } else if (!function.equals(other.function)) {
      return false;
    }
    if (head == null) {
      if (other.head != null) {
        return false;
      }
    } else if (!head.equals(other.head)) {
      return false;
    }
    if (p == null) {
      if (other.p != null) {
        return false;
      }
    } else if (!p.equals(other.p)) {
      return false;
    }
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
