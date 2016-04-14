package org.roda.core.data.v2;

import org.roda.core.data.v2.index.IsIndexed;

public abstract class NamedIndexedModel implements IsIndexed {

  private static final long serialVersionUID = 6489779852980849279L;

  private String id;
  private String name;

  protected NamedIndexedModel() {
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

  @Override
  public String getUUID() {
    return getId();
  }

}
