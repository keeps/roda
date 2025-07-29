/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationRelationOptions implements Serializable {

  @Serial
  private static final long serialVersionUID = 2928976636495410488L;

  @JsonProperty("translations")
  private Map<String, Map<String ,String>> relationsTranslations = new HashMap<>();

  public RepresentationInformationRelationOptions() {
    // empty constructor
  }

  public Map<String, Map<String ,String>> getRelationsTranslations() {
    return relationsTranslations;
  }

  public void setRelationsTranslations(Map<String, Map<String ,String>> relationsTranslations) {
    this.relationsTranslations = relationsTranslations;
  }
}
