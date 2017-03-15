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
@XmlRootElement(name = RodaConstants.RODA_OBJECT_MEMBERS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RODAMembers implements RODAObjectList<RODAMember> {
  private static final long serialVersionUID = 5656464074709994370L;
  private List<RODAMember> members;

  public RODAMembers() {
    super();
    members = new ArrayList<>();
  }

  public RODAMembers(List<RODAMember> members) {
    super();
    this.members = members;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_MEMBERS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_MEMBER)
  public List<RODAMember> getObjects() {
    return members;
  }

  @Override
  public void setObjects(List<RODAMember> members) {
    this.members = members;
  }

  @Override
  public void addObject(RODAMember member) {
    this.members.add(member);
  }

}
