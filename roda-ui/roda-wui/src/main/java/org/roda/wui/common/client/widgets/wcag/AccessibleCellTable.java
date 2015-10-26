/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class AccessibleCellTable<T> extends CellTable<T> {

  public AccessibleCellTable(String summary) {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, String summary) {
    super(pageSize);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, Resources resources, String summary) {
    super(pageSize, resources);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, ProvidesKey<T> keyProvider, String summary) {
    super(pageSize, keyProvider);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, Resources resources, ProvidesKey<T> keyProvider, String summary) {
    super(pageSize, resources, keyProvider);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, Resources resources, ProvidesKey<T> keyProvider, Widget w, String summary) {
    super(pageSize, resources, keyProvider, w);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }

  public AccessibleCellTable(int pageSize, Resources resources, ProvidesKey<T> keyProvider, Widget w, boolean b1,
    boolean b2, String summary) {
    super(pageSize, resources, keyProvider, w, b1, b2);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
    WCAGUtilities.addAttributeIfNonExistent(this.getElement(), "summary", summary);
  }
}
