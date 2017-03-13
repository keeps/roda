package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.ElementBuilderBase;
import com.google.gwt.dom.builder.shared.StylesBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;

public class AccessibleHeaderOrFooterBuilder<T> extends AbstractHeaderOrFooterBuilder<T> {

  /**
   * Create a new DefaultHeaderBuilder for the header of footer section.
   * 
   * @param table
   *          the table being built
   * @param isFooter
   *          true if building the footer, false if the header
   */
  private boolean footer;

  public AccessibleHeaderOrFooterBuilder(AbstractCellTable<T> table, boolean isFooter) {
    super(table, isFooter);
    this.footer = isFooter;
  }

  /**
   * Append the extra style names for the header.
   * 
   * @param header
   *          the header that may contain extra styles, it can be null
   * @param classesBuilder
   *          the string builder for the TD classes
   */
  private <H> void appendExtraStyles(Header<H> header, StringBuilder classesBuilder) {
    if (header == null) {
      return;
    }
    String headerStyleNames = header.getHeaderStyleNames();
    if (headerStyleNames != null) {
      classesBuilder.append(" ");
      classesBuilder.append(headerStyleNames);
    }
  }

  @Override
  protected boolean buildHeaderOrFooterImpl() {
    AbstractCellTable<T> table = getTable();
    boolean isFooter = isBuildingFooter();

    // Early exit if there aren't any columns to render.
    int columnCount = table.getColumnCount();
    if (columnCount == 0) {
      // Nothing to render;
      return false;
    }

    // Early exit if there aren't any headers in the columns to render.
    boolean hasHeader = false;
    for (int i = 0; i < columnCount; i++) {
      if (getHeader(i) != null) {
        hasHeader = true;
        break;
      }
    }
    if (!hasHeader) {
      return false;
    }

    // Get information about the sorted column.
    ColumnSortList sortList = table.getColumnSortList();
    ColumnSortInfo sortedInfo = (sortList.size() == 0) ? null : sortList.get(0);
    Column<?, ?> sortedColumn = (sortedInfo == null) ? null : sortedInfo.getColumn();
    boolean isSortAscending = (sortedInfo == null) ? false : sortedInfo.isAscending();

    // Get the common style names.
    Style style = getTable().getResources().style();
    String className = isBuildingFooter() ? style.footer() : style.header();
    String sortableStyle = " " + style.sortableHeader();
    String sortedStyle = " " + (isSortAscending ? style.sortedHeaderAscending() : style.sortedHeaderDescending());

    // Setup the first column.
    Header<?> prevHeader = getHeader(0);
    Column<T, ?> column = getTable().getColumn(0);
    int prevColspan = 1;
    boolean isSortable = false;
    boolean isSorted = false;
    StringBuilder classesBuilder = new StringBuilder(className);
    classesBuilder.append(" " + (isFooter ? style.firstColumnFooter() : style.firstColumnHeader()));
    if (!isFooter && column.isSortable()) {
      isSortable = true;
      isSorted = (column == sortedColumn);
    }

    // Loop through all column headers.
    TableRowBuilder tr = startRow();
    int curColumn;
    for (curColumn = 1; curColumn < columnCount; curColumn++) {
      Header<?> header = getHeader(curColumn);

      if (header != prevHeader) {
        // The header has changed, so append the previous one.
        if (isSortable) {
          classesBuilder.append(sortableStyle);
        }
        if (isSorted) {
          classesBuilder.append(sortedStyle);
        }
        appendExtraStyles(prevHeader, classesBuilder);

        // Render the header.
        TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
        enableColumnHandlers(th, column);
        if (prevHeader != null) {
          // Build the header.
          Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
          // Add div element with aria button role
          if (isSortable) {
            // TODO: Figure out aria-label and translation of label text
            th.attribute("role", "button");
            th.tabIndex(-1);
          }
          updatedRenderSortableHeader(th, context, prevHeader, isSorted, isSortAscending);
        }
        th.endTH();

        // Reset the previous header.
        prevHeader = header;
        prevColspan = 1;
        classesBuilder = new StringBuilder(className);
        isSortable = false;
        isSorted = false;
      } else {
        // Increment the colspan if the headers == each other.
        prevColspan++;
      }

      // Update the sorted state.
      column = table.getColumn(curColumn);
      if (!isFooter && column.isSortable()) {
        isSortable = true;
        isSorted = (column == sortedColumn);
      }
    }

    // Append the last header.
    if (isSortable) {
      classesBuilder.append(sortableStyle);
    }
    if (isSorted) {
      classesBuilder.append(sortedStyle);
    }

    // The first and last columns could be the same column.
    classesBuilder.append(" ").append(isFooter ? style.lastColumnFooter() : style.lastColumnHeader());
    appendExtraStyles(prevHeader, classesBuilder);

    // Render the last header.
    TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
    enableColumnHandlers(th, column);
    if (prevHeader != null) {
      Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
      updatedRenderSortableHeader(th, context, prevHeader, isSorted, isSortAscending);
    }
    th.endTH();

    // End the row.
    tr.endTR();

    return true;
  }

  protected final void updatedRenderSortableHeader(ElementBuilderBase<?> out, Context context, Header<?> header,
    boolean isSorted, boolean isSortAscending) {
    ElementBuilderBase<?> headerContainer = out;

    // Wrap the header in a sort icon if sorted.
    boolean isSortedAndNotFooter = isSorted && !footer;
    if (isSortedAndNotFooter) {
      // Determine the position of the sort icon.
      boolean posRight = LocaleInfo.getCurrentLocale().isRTL() ? isSortIconStartOfLine() : !isSortIconStartOfLine();

      // Create an outer container to hold the icon and the header.
      int iconWidth = isSortAscending ? getTable().getResources().sortAscending().getWidth() + 6
        : getTable().getResources().sortDescending().getWidth() + 6;
      int halfHeight = isSortAscending ? (int) Math.round(getTable().getResources().sortAscending().getHeight() / 2.0)
        : (int) Math.round(getTable().getResources().sortDescending().getHeight() / 2.0);
      DivBuilder outerDiv = out.startDiv();
      StylesBuilder style = outerDiv.style().position(Position.RELATIVE).trustedProperty("zoom", "1");
      if (posRight) {
        style.paddingRight(iconWidth, Unit.PX);
      } else {
        style.paddingLeft(iconWidth, Unit.PX);
      }
      style.endStyle();

      // Add the icon.
      DivBuilder imageHolder = outerDiv.startDiv();
      style = outerDiv.style().position(Position.ABSOLUTE).top(50.0, Unit.PCT).lineHeight(0.0, Unit.PX)
        .marginTop(-halfHeight, Unit.PX);
      if (posRight) {
        style.right(0, Unit.PX);
      } else {
        style.left(0, Unit.PX);
      }

      style.endStyle();
      imageHolder.html(getSortingIcon(isSortAscending));
      imageHolder.endDiv();

      // Create the header wrapper.
      headerContainer = outerDiv.startDiv();
    }

    // Build the header.
    renderHeader(headerContainer, context, header);

    // Close the elements used for the sort icon.
    if (isSortedAndNotFooter) {
      headerContainer.endDiv(); // headerContainer.
      headerContainer.endDiv(); // outerDiv
    }
  }

  private SafeHtml getSortingIcon(boolean isAscending) {
    AbstractCellTable<T> table = getTable();
    SafeHtmlBuilder shb = new SafeHtmlBuilder();

    if (isAscending) {
      table.getResources().sortAscending();
      shb.appendEscaped("A");
    } else {
      table.getResources().sortDescending();
      shb.appendEscaped("D");
    }

    return shb.toSafeHtml();
  }

}
