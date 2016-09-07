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

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "members")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RODAMembers implements RODAObjectList<RODAMember> {
  private List<RODAMember> members;

  public RODAMembers() {
    super();
    members = new ArrayList<RODAMember>();
  }

  public RODAMembers(List<RODAMember> members) {
    super();
    this.members = members;
  }

  @JsonProperty(value = "members")
  @XmlElement(name = "member")
  public List<RODAMember> getObjects() {
    return members;
  }

  public void setObjects(List<RODAMember> members) {
    this.members = members;
  }

  public void addObject(RODAMember member) {
    this.members.add(member);
  }

}
