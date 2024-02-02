/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_OTHERS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectPermissionResult {
  private List<ObjectPermission> objectPermissions;

  public ObjectPermissionResult() {
    super();
    objectPermissions = new ArrayList<>();
  }

  public ObjectPermissionResult(List<ObjectPermission> objectPermissions) {
    super();
    this.objectPermissions = objectPermissions;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_OTHER)
  @XmlElement(name = RodaConstants.RODA_OBJECT_PERMISSION)
  public List<ObjectPermission> getObjects() {
    return objectPermissions;
  }

  public void setObjects(List<ObjectPermission> objectPermissions) {
    this.objectPermissions = objectPermissions;
  }

  public void addObject(ObjectPermission objectPermission) {
    this.objectPermissions.add(objectPermission);
  }

}
