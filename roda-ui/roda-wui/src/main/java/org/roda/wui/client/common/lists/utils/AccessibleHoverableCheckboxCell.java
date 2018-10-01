/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.roda.wui.client.common.lists.utils;

import java.util.function.Consumer;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Copy of {@link CheckboxCell} with modifications to support mouseover events.
 */
public class AccessibleHoverableCheckboxCell extends AbstractEditableCell<Boolean, Boolean> {

  /**
   * An html string representation of a checked input box.
   */
  private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils
    .fromSafeConstant("<input title=\"selectItem\" type=\"checkbox\" tabindex=\"-1\" checked/>");

  /**
   * An html string representation of an unchecked input box.
   */
  private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
    .fromSafeConstant("<input title=\"selectItem\" type=\"checkbox\" tabindex=\"-1\"/>");

  private final boolean dependsOnSelection;
  private final boolean handlesSelection;
  private Consumer<Element> onMouseOver = null;
  private Consumer<Element> onMouseOut = null;

  private InputElement input = null;

  public AccessibleHoverableCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
    super(BrowserEvents.CHANGE, BrowserEvents.KEYDOWN, BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT);
    this.dependsOnSelection = dependsOnSelection;
    this.handlesSelection = handlesSelection;
  }

  @Override
  public boolean dependsOnSelection() {
    return dependsOnSelection;
  }

  @Override
  public boolean handlesSelection() {
    return handlesSelection;
  }

  @Override
  public boolean isEditing(Context context, Element parent, Boolean value) {
    return false;
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event,
    ValueUpdater<Boolean> valueUpdater) {
    String type = event.getType();
    input = parent.getFirstChild().cast();

    boolean enterPressed = BrowserEvents.KEYDOWN.equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;

    if (BrowserEvents.CHANGE.equals(type) || enterPressed) {
      Boolean isChecked = input.isChecked();

      if (enterPressed && (handlesSelection() || !dependsOnSelection())) {
        isChecked = !isChecked;
        input.setChecked(isChecked);
      }

      if (value != isChecked && !dependsOnSelection()) {
        setViewData(context.getKey(), isChecked);
      } else {
        clearViewData(context.getKey());
      }

      if (valueUpdater != null) {
        GWT.log("valueUpdater!!");
        valueUpdater.update(isChecked);
      }
    } else if (BrowserEvents.MOUSEOUT.equals(type) && onMouseOut != null) {
      onMouseOut.accept(parent);
    } else if (BrowserEvents.MOUSEOVER.equals(type) && onMouseOver != null) {
      onMouseOver.accept(parent);
    }
  }

  @Override
  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
    // Get the view data.
    Object key = context.getKey();
    Boolean viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    if (value != null && ((viewData != null) ? viewData : value)) {
      sb.append(INPUT_CHECKED);
    } else {
      sb.append(INPUT_UNCHECKED);
    }
  }

  public void setMouseOverHandlers(Consumer<Element> onMouseOver, Consumer<Element> onMouseOut) {
    this.onMouseOver = onMouseOver;
    this.onMouseOut = onMouseOut;
  }
}
