/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.dissemination.browse.client;

import java.io.Serializable;

public class DescriptiveMetadataEditBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private String id;
  private String type;
  private String xml;

  public DescriptiveMetadataEditBundle() {
    super();
  }

  public DescriptiveMetadataEditBundle(String id, String type, String xml) {
    super();
    this.id = id;
    this.type = type;
    this.xml = xml;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  
}
