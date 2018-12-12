/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_GROUPS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RODAGroups implements RODAObjectList<Group> {
  private static final long serialVersionUID = 5656464074709994370L;
  private List<Group> groups;

  public RODAGroups() {
    super();
    groups = new ArrayList<>();
  }

  public RODAGroups(List<Group> groups) {
    super();
    this.groups = groups;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_GROUPS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_GROUP)
  public List<Group> getObjects() {
    return groups;
  }

  @Override
  public void setObjects(List<Group> groups) {
    this.groups = groups;
  }

  @Override
  public void addObject(Group group) {
    this.groups.add(group);
  }

}
