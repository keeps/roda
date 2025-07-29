/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.properties;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ReindexPluginObject implements Serializable {
  @Serial
  private static final long serialVersionUID = -4195049745164131592L;

  private String simpleName;
  private String name;

  public ReindexPluginObject() {
    // empty constructor
  }

  public ReindexPluginObject(String simpleName, String name) {
    this.simpleName = simpleName;
    this.name = name;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public void setSimpleName(String simpleName) {
    this.simpleName = simpleName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
