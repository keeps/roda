package org.roda.core.data.v2.disposal.confirmation;

import org.roda.core.data.v2.generics.MetadataValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationForm implements Serializable {

  @Serial
  private static final long serialVersionUID = -7169481944091305485L;

  private Set<MetadataValue> values;

  public DisposalConfirmationForm() {
    super();
  }

  public DisposalConfirmationForm(Set<MetadataValue> values) {
    super();
    this.values = values;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }
}
