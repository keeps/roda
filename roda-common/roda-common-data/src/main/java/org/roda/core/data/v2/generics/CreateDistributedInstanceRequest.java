package org.roda.core.data.v2.generics;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateDistributedInstanceRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String name;
  private String description;

  public CreateDistributedInstanceRequest() {
    // empty constructor
  }

  public CreateDistributedInstanceRequest(String name, String description) {
    this.name = name;
    this.description = description;
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
