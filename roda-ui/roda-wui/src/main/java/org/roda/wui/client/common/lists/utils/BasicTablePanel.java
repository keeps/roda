package org.roda.wui.client.common.lists.utils;

import java.util.Iterator;
import java.util.List;

import org.roda.wui.common.client.widgets.MyCellTableResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class BasicTablePanel<C> extends Composite {
  private static BasicTablePanelUiBinder uiBinder = GWT.create(BasicTablePanelUiBinder.class);
  private final SingleSelectionModel<C> selectionModel;
  @UiField
  SimplePanel header;
  @UiField
  SimplePanel info;
  @UiField
  SimplePanel table;
  private ListDataProvider<C> dataProvider;
  private CellTable<C> display;
  private ScrollPanel displayScroll;
  private SimplePanel displayScrollWrapper;

  @SafeVarargs
  public BasicTablePanel(Widget headerContent, SafeHtml infoContent, Iterator<C> rowItems, ColumnInfo<C>... columns) {
    this(headerContent, new HTMLPanel(infoContent), GWT.create(MyCellTableResources.class), rowItems, columns);
  }

  @SafeVarargs
  public BasicTablePanel(Widget headerContent, SafeHtml infoContent, CellTable.Resources resources,
    Iterator<C> rowItems, ColumnInfo<C>... columns) {
    this(headerContent, new HTMLPanel(infoContent), resources, rowItems, columns);
  }

  @SafeVarargs
  public BasicTablePanel(Widget headerContent, Widget infoContent, Iterator<C> rowItems, ColumnInfo<C>... columns) {
    this(headerContent, infoContent, GWT.create(MyCellTableResources.class), rowItems, columns);
  }

  @SafeVarargs
  public BasicTablePanel(Widget headerContent, Widget infoContent, CellTable.Resources resources, Iterator<C> rowItems,
    ColumnInfo<C>... columns) {
    initWidget(uiBinder.createAndBindUi(this));

    // set widgets
    header.setWidget(headerContent);
    info.setWidget(infoContent);

    display = createTable(resources, rowItems, columns);
    selectionModel = new SingleSelectionModel<>();
    display.setSelectionModel(selectionModel);

    displayScroll = new ScrollPanel(display);
    displayScrollWrapper = new SimplePanel(displayScroll);
    displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper");
    table.setWidget(displayScrollWrapper);

    displayScroll.addScrollHandler(event -> handleScrollChanges());
    handleScrollChanges();
  }

  public BasicTablePanel(Widget headerContent, String infoContent) {
    initWidget(uiBinder.createAndBindUi(this));

    // set widgets
    header.setWidget(headerContent);

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
    b.append(SafeHtmlUtils.fromString(infoContent));
    b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    info.setWidget(new HTMLPanel(b.toSafeHtml()));

    table.setVisible(false);
    selectionModel = null;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    handleScrollChanges();
  }

  public void handleScrollChanges() {
    if (displayScroll.getMaximumHorizontalScrollPosition() > 0) {
      double percent = displayScroll.getHorizontalScrollPosition() * 100F
        / displayScroll.getMaximumHorizontalScrollPosition();

      com.google.gwt.core.shared.GWT.log(String.valueOf(percent));

      if (percent > 0) {
        // show left shadow
        displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      } else {
        // hide left shadow
        displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      }

      if (percent < 100) {
        // show right shadow
        displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
      } else {
        // hide right shadow
        displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
      }
    } else {
      // hide both shadows
      displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
    }
  }

  @SafeVarargs
  private final CellTable<C> createTable(CellTable.Resources resources, Iterator<C> rowItems,
    ColumnInfo<C>... columns) {
    CellTable<C> cellTable = new CellTable<>(Integer.MAX_VALUE, resources);
    cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    cellTable.setLoadingIndicator(new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));
    cellTable.addStyleName("table-info my-asyncdatagrid-display");

    // add columns
    for (ColumnInfo<C> column : columns) {
      if (!column.hide) {
        cellTable.addColumn(column.column, column.header);
        if (column.widthEM > 0) {
          cellTable.setColumnWidth(column.column, column.widthEM, Style.Unit.EM);
        }
      }
    }

    // fetch rows
    dataProvider = new ListDataProvider<C>();
    dataProvider.addDataDisplay(cellTable);
    List<C> list = dataProvider.getList();
    while (rowItems.hasNext()) {
      C rowItem = rowItems.next();
      list.add(rowItem);
    }

    return cellTable;
  }

  public SingleSelectionModel<C> getSelectionModel() {
    return selectionModel;
  }

  public CellTable<C> getDisplay() {
    return display;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (this.getSelectionModel() != null) {
      this.getSelectionModel().clear();
    }
  }

  public ListDataProvider<C> getDataProvider() {
    return dataProvider;
  }

  interface BasicTablePanelUiBinder extends UiBinder<Widget, BasicTablePanel> {
  }

  public static class ColumnInfo<C> {
    private Column<C, ?> column;
    private double widthEM;
    private SafeHtml header;
    private boolean hide;

    public ColumnInfo(SafeHtml header, boolean hide, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this.header = header;
      this.widthEM = widthEM;
      this.column = column;
      this.hide = hide;
      for (String addCellStyleName : addCellStyleNames) {
        this.column.setCellStyleNames(addCellStyleName);
      }
    }

    public ColumnInfo(String header, boolean hide, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this(SafeHtmlUtils.fromString(header), hide, widthEM, column, addCellStyleNames);
    }

    public ColumnInfo(String header, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this(SafeHtmlUtils.fromString(header), false, widthEM, column, addCellStyleNames);
    }
  }
}
