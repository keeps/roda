/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.List;

public class Viewer implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;

  private String type;
  private List<String> pronoms;
  private List<String> mimetypes;
  private List<String> extensions;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getPronoms() {
    return pronoms;
  }

  public void setPronoms(List<String> pronoms) {
    this.pronoms = pronoms;
  }

  public List<String> getMimetypes() {
    return mimetypes;
  }

  public void setMimetypes(List<String> mimetypes) {
    this.mimetypes = mimetypes;
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<String> extensions) {
    this.extensions = extensions;
  }

  @Override
  public String toString() {
    return "Viewer [type=" + type + ", pronoms=" + String.join(",", pronoms) + ", mimetypes="
      + String.join(",", mimetypes) + ", extensions=" + String.join(",", extensions) + "]";
  }

}
