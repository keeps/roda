/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.accessKey;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessKeys implements RODAObjectList<AccessKey> {

  @Serial
  private static final long serialVersionUID = -7954551447492822643L;
  private List<AccessKey> accessKeyList;

  public AccessKeys() {
    super();
    accessKeyList = new ArrayList<>();
  }

  public AccessKeys(List<AccessKey> accessKeys) {
    super();
    this.accessKeyList = accessKeys;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_ACCESS_KEYS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_ACCESS_KEY)
  public List<AccessKey> getObjects() {
    return accessKeyList;
  }

  @Override
  public void setObjects(List<AccessKey> accessKeys) {
    this.accessKeyList = accessKeys;
  }

  @Override
  public void addObject(AccessKey accessKey) {
    this.accessKeyList.add(accessKey);
  }

  @JsonIgnore
  public AccessKey getAccessKeyByKey(String key) {
    for (AccessKey accessKey : accessKeyList) {
      if (accessKey.getKey().equals(key)) {
        return accessKey;
      }
    }
    return null;
  }
}
