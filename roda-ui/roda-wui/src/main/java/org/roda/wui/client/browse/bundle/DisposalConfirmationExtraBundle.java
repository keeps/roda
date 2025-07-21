/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationExtraBundle implements Bundle {
  private static final long serialVersionUID = -7169481944091305485L;

  private Set<MetadataValue> values;

  public DisposalConfirmationExtraBundle() {
    super();
  }

  public DisposalConfirmationExtraBundle(Set<MetadataValue> values) {
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
