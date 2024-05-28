package org.roda.core.data.v2.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.v2.generics.MetadataValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateUserRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private User user;
  private SecureString password;
  private Set<MetadataValue> values;

  public CreateUserRequest(User user, SecureString password, Set<MetadataValue> values) {
    this.user = user;
    this.password = password;
    this.values = values;
  }

  public CreateUserRequest() {
    this.user = new User();
    this.password = new SecureString();
    this.values = new HashSet<>();
  }

  public User getUser() {
    return user;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public SecureString getPassword() {
    return password;
  }

  public void setPassword(SecureString password) {
    this.password = password;
  }
}
