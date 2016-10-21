/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.HashMap;

public class Viewers implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;

  private HashMap<String, String> pronoms;
  private HashMap<String, String> mimetypes;
  private HashMap<String, String> extensions;
  private String textLimit;

  public Viewers() {
    this.pronoms = new HashMap<String, String>();
    this.mimetypes = new HashMap<String, String>();
    this.extensions = new HashMap<String, String>();
    this.textLimit = "";
  }

  public HashMap<String, String> getPronoms() {
    return pronoms;
  }

  public void setPronoms(HashMap<String, String> pronoms) {
    this.pronoms = pronoms;
  }

  public HashMap<String, String> getMimetypes() {
    return mimetypes;
  }

  public void setMimetypes(HashMap<String, String> mimetypes) {
    this.mimetypes = mimetypes;
  }

  public HashMap<String, String> getExtensions() {
    return extensions;
  }

  public void setExtensions(HashMap<String, String> extensions) {
    this.extensions = extensions;
  }

  public void addPronom(String pronom, String type) {
    this.pronoms.put(pronom, type);
  }

  public void addMimetype(String mimetype, String type) {
    this.mimetypes.put(mimetype, type);
  }

  public void addExtension(String extension, String type) {
    this.extensions.put(extension, type);
  }

  public String getTextLimit() {
    return textLimit;
  }

  public void setTextLimit(String textLimit) {
    this.textLimit = textLimit;
  }

  @Override
  public String toString() {
    return "Viewer [pronoms=" + pronoms + ", mimetypes=" + mimetypes + ", extensions=" + extensions + ", textLimit="
      + textLimit + "]";
  }
}
