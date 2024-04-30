package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationCustomForm implements Serializable {
  @Serial
  private static final long serialVersionUID = 1768436615696712964L;

  private Set<MetadataValue> values;

  public RepresentationInformationCustomForm() {
    super();
  }

  public RepresentationInformationCustomForm(Set<MetadataValue> values) {
    super();
    this.values = values;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "RepresentationInformationCustomForm{" +
        "values=" + values +
        '}';
  }
}
