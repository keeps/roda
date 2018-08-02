/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.util.function.Supplier;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.Actionable;

public class ListBuilder<T extends IsIndexed> {
  private final AsyncTableCell.Options<T> options;
  private final Supplier<AsyncTableCell<T>> listSupplier;
  private Actionable<T> actionable = null;

  public ListBuilder(Supplier<AsyncTableCell<T>> listSupplier, AsyncTableCell.Options<T> options) {
    this.options = options;
    this.listSupplier = listSupplier;
  }

  public AsyncTableCell<T> build() {
    if (actionable != null) {
      options.withActionable(actionable);
    }
    return listSupplier.get().initialize(options);
  }

  public AsyncTableCell.Options<T> getOptions() {
    return options;
  }
}
