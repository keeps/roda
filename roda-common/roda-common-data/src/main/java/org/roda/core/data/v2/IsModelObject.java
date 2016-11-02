package org.roda.core.data.v2;

public interface IsModelObject extends IsRODAObject {
  /**
   * 20161102 hsilva: a <code>@JsonIgnore</code> should be added to avoid
   * serializing
   */
  public int getClassVersion();
}
