/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.accessKey;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_ACCESS_KEYS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessKeys implements RODAObjectList<AccessKey> {
  private static final long serialVersionUID = -7954551447492822643L;
  private List<AccessKey> accessKeys;

  public AccessKeys() {
    super();
    accessKeys = new ArrayList<>();
  }

  public AccessKeys(List<AccessKey> accessKeys) {
    super();
    this.accessKeys = accessKeys;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_ACCESS_KEYS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_ACCESS_KEY)
  public List<AccessKey> getObjects() {
    return accessKeys;
  }

  @Override
  public void setObjects(List<AccessKey> accessKeys) {
    this.accessKeys = accessKeys;
  }

  @Override
  public void addObject(AccessKey accessKey) {
    this.accessKeys.add(accessKey);
  }

  @JsonIgnore
  public AccessKey getAccessKeyByKey(String key) {
    for (AccessKey accessKey : accessKeys) {
      if (accessKey.getKey().equals(key)) {
        return accessKey;
      }
    }
    return null;
  }
}
