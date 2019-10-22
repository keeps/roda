/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.io.Serializable;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_PERMISSION)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ObjectPermission implements Serializable {
  private static final long serialVersionUID = -6473833736054473793L;

  private String objectClass = null;
  private String objectId = null;
  private boolean hasPermission = false;

  public ObjectPermission() {
    super();
  }

  public ObjectPermission(String objectClass, String objectId, boolean hasPermission) {
    super();
    this.objectClass = objectClass;
    this.objectId = objectId;
    this.hasPermission = hasPermission;
  }

  public ObjectPermission(ObjectPermission objectPermission) {
    super();
    this.objectClass = objectPermission.getObjectClass();
    this.objectId = objectPermission.getObjectId();
    this.hasPermission = objectPermission.isHasPermission();
  }

  public String getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(String objectClass) {
    this.objectClass = objectClass;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public boolean isHasPermission() {
    return hasPermission;
  }

  public void setHasPermission(boolean hasPermission) {
    this.hasPermission = hasPermission;
  }

}
