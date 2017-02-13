/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import config.i18n.client.ClientMessages;

public class AccessibleSimplePager extends SimplePager {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, boolean showLastPageButton) {
    super(location, showFastForwardButton, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager() {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location) {
    super(location);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows,
    boolean showLastPageButton) {
    super(location, showFastForwardButton, fastForwardRows, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location, SimplePager.Resources resources, boolean showFastForwardButton,
    int fastForwardRows, boolean showLastPageButton, boolean showFirstPageButton,
    SimplePager.ImageButtonsConstants imageButtonConstants) {
    super(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton, showFirstPageButton,
      imageButtonConstants);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  @Override
  protected String createText() {
    // Default text is 1 based.
    NumberFormat formatter = NumberFormat.getFormat("#,###");
    HasRows display = getDisplay();
    Range range = display.getVisibleRange();
    int pageStart = range.getStart() + 1;
    int pageSize = range.getLength();
    int dataSize = display.getRowCount();
    int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
    endIndex = Math.max(pageStart, endIndex);
    boolean exact = display.isRowCountExact();
    return formatter.format(pageStart) + "-" + formatter.format(endIndex) + " "
      + (exact ? messages.of() : messages.ofOver()) + " " + formatter.format(dataSize);
  }

}
