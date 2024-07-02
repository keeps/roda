package org.roda.core.data.v2.synchronization.central;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class UpdateDistributedInstanceRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 5597476107465354723L;

  private String id;
  private String name;
  private String description;

  public UpdateDistributedInstanceRequest() {
    // empty constructor
  }

  public UpdateDistributedInstanceRequest(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
