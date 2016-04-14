package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.IsIndexed;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface SelectDialog extends HasValueChangeHandlers<IsIndexed> {

  public void showAndCenter();

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IsIndexed> changeHandler);

}
