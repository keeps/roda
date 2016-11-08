/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import config.i18n.client.ClientMessages;

/**
 * A simple pager that controls the page size.
 * 
 * NOTE: GWT 2.7 PageSizePager doesn't have the ShowMore and ShowLess button localized.
 */
public class RodaPageSizePager extends AbstractPager {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  /**
   * The increment by which to grow or shrink the page size.
   */
  private final int increment;

  /**
   * The main layout widget.
   */
  private final FlexTable layout = new FlexTable();

  private final Anchor showMoreButton;
  private final Anchor showLessButton;

  /**
   * Construct a PageSizePager with a given increment.
   * 
   * @param increment the amount by which to increase the page size
   */
  @UiConstructor
  public RodaPageSizePager(final int increment) {
    showMoreButton = new Anchor(messages.showMore());
    showLessButton = new Anchor(messages.showLess());
    this.increment = increment;
    initWidget(layout);
    layout.setCellPadding(0);
    layout.setCellSpacing(0);

    // Show more button.
    showMoreButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // Display should be non-null, but we check defensively.
        HasRows display = getDisplay();
        if (display != null) {
          Range range = display.getVisibleRange();
          int pageSize = Math.min(range.getLength() + increment,
              display.getRowCount()
                  + (display.isRowCountExact() ? 0 : increment));
          display.setVisibleRange(range.getStart(), pageSize);
        }
      }
    });
    showLessButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // Display should be non-null, but we check defensively.
        HasRows display = getDisplay();
        if (display != null) {
          Range range = display.getVisibleRange();
          int pageSize = Math.max(range.getLength() - increment, increment);
          display.setVisibleRange(range.getStart(), pageSize);
        }
      }
    });

    // Add the buttons to the pager.
    layout.setWidget(0, 0, showLessButton);
    layout.setText(0, 1, " | ");
    layout.setWidget(0, 2, showMoreButton);

    // Hide the buttons by default.
    setDisplay(null);
  }

  @Override
  public void setDisplay(HasRows display) {
    // Hide the buttons if the display is null. If the display is non-null, the
    // buttons will be displayed in onRangeOrRowCountChanged().
    if (display == null) {
      showLessButton.setVisible(false);
      showMoreButton.setVisible(false);
    }
    super.setDisplay(display);
  }

  @Override
  public void setPageSize(int pageSize) {
    super.setPageSize(pageSize);
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    // Assumes a page start index of 0.
    HasRows display = getDisplay();
    int pageSize = display.getVisibleRange().getLength();
    boolean hasLess = pageSize > increment;
    boolean hasMore = !display.isRowCountExact()
        || pageSize < display.getRowCount();
    showLessButton.setVisible(hasLess);
    showMoreButton.setVisible(hasMore);
    layout.setText(0, 1, (hasLess && hasMore) ? " | " : "");
  }

  /**
   * Visible for testing.
   */
  boolean isShowLessButtonVisible() {
    return showLessButton.isVisible();
  }

  /**
   * Visible for testing.
   */
  boolean isShowMoreButtonVisible() {
    return showMoreButton.isVisible();
  }
}