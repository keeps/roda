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
@JsonTypeName("SelectedItemsAllRequest")
public class SelectedItemsAllRequest implements SelectedItemsRequest {
  @Serial
  private static final long serialVersionUID = 165811248428623687L;

  public SelectedItemsAllRequest() {
    // do nothing
  }
}
