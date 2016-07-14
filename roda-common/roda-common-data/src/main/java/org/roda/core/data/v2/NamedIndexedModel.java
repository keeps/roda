/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;

public abstract class NamedIndexedModel implements Serializable {

  private static final long serialVersionUID = 6489779852980849279L;

  private String id;
  private String name;

  public NamedIndexedModel() {
    super();
    this.id = null;
    this.name = null;
  }

  protected NamedIndexedModel(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public abstract String getUUID();
}
