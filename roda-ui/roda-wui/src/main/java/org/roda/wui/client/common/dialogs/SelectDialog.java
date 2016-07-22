/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.IsIndexed;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface SelectDialog<T extends IsIndexed> extends HasValueChangeHandlers<T> {

  public void showAndCenter();

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> changeHandler);

}
