/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.generics.select;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonTypeName("SelectedItemsNoneRequest")
public class SelectedItemsNoneRequest implements SelectedItemsRequest {
  @Serial
  private static final long serialVersionUID = 4225008000954460297L;

  public SelectedItemsNoneRequest() {
    // do nothing
  }
}
