package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationFamily implements Serializable {
  @Serial
  private static final long serialVersionUID = 6699642580798560801L;

  private Set<MetadataValue> familyValues = new HashSet<>();

  public RepresentationInformationFamily() {
    // empty constructor
  }

  public Set<MetadataValue> getFamilyValues() {
    return familyValues;
  }

  public void setFamilyValues(Set<MetadataValue> familyValues) {
    this.familyValues = familyValues;
  }
}
