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


import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_USERS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RODAUsers implements RODAObjectList<User> {
  private static final long serialVersionUID = 5656464074709994370L;
  private List<User> users;

  public RODAUsers() {
    super();
    users = new ArrayList<>();
  }

  public RODAUsers(List<User> users) {
    super();
    this.users = users;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_USERS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_USER, nillable = true)
  public List<User> getObjects() {
    return users;
  }

  @Override
  public void setObjects(List<User> users) {
    this.users = users;
  }

  @Override
  public void addObject(User user) {
    this.users.add(user);
  }

}
