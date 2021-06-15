package org.roda.core.data.v2.accessToken;

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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_ACCESS_TOKENS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessTokens implements RODAObjectList<AccessToken> {
  private static final long serialVersionUID = -7954551447492822643L;
  private List<AccessToken> accessTokens;

  public AccessTokens() {
    super();
    accessTokens = new ArrayList<>();
  }

  public AccessTokens(List<AccessToken> accessTokens) {
    super();
    this.accessTokens = accessTokens;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_ACCESS_TOKENS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_ACCESS_TOKEN)
  public List<AccessToken> getObjects() {
    return accessTokens;
  }

  @Override
  public void setObjects(List<AccessToken> accessTokens) {
    this.accessTokens = accessTokens;
  }

  @Override
  public void addObject(AccessToken accessToken) {
    this.accessTokens.add(accessToken);
  }

  @JsonIgnore
  public AccessToken getAccessTokenByKey(String key) {
    for (AccessToken accessToken : accessTokens) {
      if (accessToken.getAccessKey().equals(key)) {
        return accessToken;
      }
    }
    return null;
  }
}
