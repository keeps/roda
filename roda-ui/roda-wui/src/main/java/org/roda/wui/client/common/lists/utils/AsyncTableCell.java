/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.collapse.Collapse;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.MyCellTableResources;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleCellTable;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.AccessibleSimplePager;
import org.roda.wui.common.client.widgets.wcag.AcessibleCheckboxCell;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractHasData.RedrawEvent;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import config.i18n.client.ClientMessages;

public abstract class AsyncTableCell<T extends IsIndexed> extends FlowPanel
  implements HasValueChangeHandlers<IndexResult<T>> {

  public static final Integer DEFAULT_INITIAL_PAGE_SIZE = 20;
  public static final Integer DEFAULT_PAGE_SIZE_INCREMENT = 100;

  static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final ClientLogger LOGGER = new ClientLogger(AsyncTableCell.class.getName());
  private final List<CheckboxSelectionListener<T>> listeners = new ArrayList<>();
  private AsyncTableCellOptions<T> options;
  private Class<T> classToReturn;
  private String listId;
  private Actionable<T> actionable;
  private FlowPanel mainPanel;
  private FlowPanel sidePanel;
  private MyAsyncDataProvider<T> dataProvider;
  private SingleSelectionModel<T> selectionModel;
  private AccessibleSimplePager resultsPager;
  private RodaPageSizePager pageSizePager;
  private CellTable<T> display;
  private RadioButton selectAllRadioButton = null;
  private Set<T> selected = new HashSet<>();
  private Filter filter;
  private boolean justActive;
  private Facets facets;
  private boolean selectable;
  private List<String> fieldsToReturn;
  private Collapse collapse;

  private HandlerRegistration facetsValueChangedHandlerRegistration;

  private int initialPageSize;
  private int pageSizeIncrement;

  private IndexResult<T> result;
  private AsyncCallback<Actionable.ActionImpact> actionableCallback = null;
  private boolean redirectOnSingleResult;
  private Filter originalFilter;
  private HandlerRegistration selectAllPopupHistoryChangedHandler = null;
  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerMillis = 0;
  private AutoUpdateState autoUpdateState = AutoUpdateState.AUTO_UPDATE_OFF;
  private AccessibleFocusPanel autoUpdatePanel;
  private InlineHTML autoUpdateSignal = new InlineHTML("");
  private HandlerRegistration autoUpdateHandler;

  public AsyncTableCell() {
    super();
  }

  // private List<Consumer<AutoUpdateState>> autoUpdateConsumers = new
  // ArrayList<>();

  AsyncTableCell<T> initialize(AsyncTableCellOptions<T> options) {
    adjustOptions(options);
    this.options = options;

    this.classToReturn = options.getClassToReturn();
    this.initialPageSize = options.getInitialPageSize();
    this.pageSizeIncrement = options.getPageSizeIncrement();
    this.listId = options.getListId();
    this.actionable = options.getActionable();
    this.actionableCallback = options.getActionableCallback();

    final String notNullSummary = StringUtils.isNotBlank(options.getSummary()) ? options.getSummary()
      : "summary" + Random.nextInt(1000);

    this.filter = options.getFilter();
    this.originalFilter = options.getFilter();
    this.justActive = options.isJustActive();
    this.facets = options.getFacets();
    this.selectable = (actionable != null && actionable.hasAnyRoles()) || options.getForceSelectable();
    this.redirectOnSingleResult = options.getRedirectOnSingleResult();

    this.fieldsToReturn = options.getFieldsToReturn();

    this.setVisible(!options.isStartHidden());

    setStylePrimaryName("my-asyncdatagrid");

    HTML loadingPanel = new HTML(HtmlSnippetUtils.LOADING);
    loadingPanel.setStylePrimaryName("my-asyncdatagrid-loading-panel");
    add(loadingPanel);

    display = new AccessibleCellTable<>(getInitialPageSize(), GWT.create(MyCellTableResources.class), getKeyProvider(),
      options.getSummary());
    display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    display.addLoadingStateChangeHandler(event -> setStyleDependentName("loading",
      LoadingStateChangeEvent.LoadingState.LOADING.equals(event.getLoadingState())));

    configure(display);

    this.dataProvider = new MyAsyncDataProvider<T>(display, fieldsToReturn, new IndexResultDataProvider<T>() {

      @Override
      public CompletableFuture<IndexResult<T>> getData(Sublist sublist, Sorter sorter, List<String> fieldsToReturn) {
        return AsyncTableCell.this.getData(sublist, sorter, fieldsToReturn).thenApply(tIndexResult -> {
          setResult(tIndexResult);
          if (redirectOnSingleResult && originalFilter.equals(AsyncTableCell.this.getFilter())
            && getVisibleItems().size() == 1) {
            HistoryUtils.resolve(getVisibleItems().get(0), true);
          }

          if (tIndexResult.getResults().isEmpty()) {
            AsyncTableCell.this.addStyleName("table-empty");
          } else {
            AsyncTableCell.this.removeStyleName("table-empty");
          }
          clearSelected();
          return tIndexResult;
        });
      }

      @Override
      public Sorter getSorter(ColumnSortList columnSortList) {
        return AsyncTableCell.this.getSorter(columnSortList);
      }
    }) {

      @Override
      protected void fireChangeEvent(IndexResult<T> result) {
        ValueChangeEvent.fire(AsyncTableCell.this, result);
      }
    };

    dataProvider.addDataDisplay(display);

    resultsPager = new AccessibleSimplePager(AccessibleSimplePager.TextLocation.LEFT,
      GWT.create(SimplePager.Resources.class), false, initialPageSize, false, false,
      GWT.create(SimplePager.ImageButtonsConstants.class));
    resultsPager.setDisplay(display);

    pageSizePager = new RodaPageSizePager(getPageSizePagerIncrement());
    pageSizePager.setDisplay(display);

    Button csvDownloadButton = new Button(messages.tableDownloadCSV());
    csvDownloadButton.addStyleName("btn btn-link csvDownloadButton");
    csvDownloadButton.setVisible(options.isCsvDownloadButtonVisibility());

    sidePanel = new FlowPanel();
    sidePanel.addStyleName("my-asyncdatagrid-side-panel");
    add(sidePanel);

    mainPanel = new FlowPanel();
    mainPanel.addStyleName("my-asyncdatagrid-main-panel");
    add(mainPanel);

    autoUpdatePanel = new AccessibleFocusPanel();
    autoUpdatePanel.add(autoUpdateSignal);

    mainPanel.add(display);
    mainPanel.add(resultsPager);
    mainPanel.add(pageSizePager);
    mainPanel.add(autoUpdatePanel);
    mainPanel.add(csvDownloadButton);

    SimplePanel clearfix = new SimplePanel();
    clearfix.addStyleName("clearfix");
    add(clearfix);

    toggleSidePanel(createAndBindFacets(sidePanel));

    csvDownloadButton.addClickHandler(event -> {
      Services services = new Services("Retrieve export limit", "get");
      services.configurationsResource(ConfigurationRestService::retrieveExportLimit)
        .whenComplete((limit, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            Toast.showInfo(messages.exportListTitle(), messages.exportListMessage(limit.getResult().intValue()));
            RestUtils.requestCSVExport(getClassToReturn(), getFilter(), dataProvider.getSorter(),
              new Sublist(0, limit.getResult().intValue()), getFacets(), getJustActive(), false,
              notNullSummary + ".csv");
          }
        });
    });

    selectionModel = new SingleSelectionModel<>(getKeyProvider());

    Handler<T> selectionEventManager = getSelectionEventManager();
    if (selectionEventManager != null) {
      display.setSelectionModel(selectionModel, selectionEventManager);
    } else {
      display.setSelectionModel(selectionModel);
    }

    display.addColumnSortHandler(new AsyncHandler(display));

    getElement().setId("list-" + listId);
    resultsPager.addStyleName("my-asyncdatagrid-pager-results");
    pageSizePager.addStyleName("my-asyncdatagrid-pager-pagesize");
    autoUpdatePanel.addStyleName("my-asyncdatagrid-autoupdate-signal");
    display.addStyleName("my-asyncdatagrid-display");

    addValueChangeHandler(new ValueChangeHandler<IndexResult<T>>() {
      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<T>> event) {
        selected = new HashSet<>();
      }
    });

    updateEmptyTableWidget();

    // nvieira 2018-07-23: needs to be improved to update a UI button instead of
    // a log
    // autoUpdateConsumers.add(st -> GWT.log(st.toString()));
    addAutoUpdateControlListener();

    if (options.isBindOpener()) {
      ListSelectionUtils.bindBrowseOpener(this);
    }

    for (CheckboxSelectionListener<T> listener : options.getCheckboxSelectionListeners()) {
      addCheckboxSelectionListener(listener);
    }

    for (ValueChangeHandler<IndexResult<T>> handler : options.getIndexResultValueChangeHandlers()) {
      addValueChangeHandler(handler);
    }

    for (SelectionChangeEvent.Handler handler : options.getSelectionChangeHandlers()) {
      getSelectionModel().addSelectionChangeHandler(handler);
    }

    for (RedrawEvent.Handler handler : options.getRedrawEventHandlers()) {
      display.addRedrawHandler(handler);
    }

    for (String extraStyleName : options.getExtraStyleNames()) {
      addStyleName(extraStyleName);
    }

    if (options.getAutoUpdate() != null) {
      autoUpdate(options.getAutoUpdate());
    }

    return this;
  }

  protected void adjustOptions(AsyncTableCellOptions<T> options) {
    // override this to add defaults or enforce rules
  }

  /**
   * @return the options
   */
  public AsyncTableCellOptions<T> getOptions() {
    return options;
  }

  private void toggleSidePanel(boolean toggle) {
    if (toggle) {
      mainPanel.removeStyleName("my-asyncdatagrid-main-panel-full");
      sidePanel.removeStyleName("my-asyncdatagrid-side-panel-hidden");
    } else {
      mainPanel.addStyleName("my-asyncdatagrid-main-panel-full");
      sidePanel.addStyleName("my-asyncdatagrid-side-panel-hidden");
    }
  }

  private void updateEmptyTableWidget() {
    FlowPanel emptyTablewidget = new FlowPanel();
    emptyTablewidget.addStyleName("table-empty-inner");
    String someOfAObject = messages.someOfAObject(getSelected().getSelectedClass());

    if (hasSelectedFacets()) {

      Label msgBeforeLink = new InlineLabel(messages.noItemsToDisplayButFacetsActive(someOfAObject) + " ");
      msgBeforeLink.addStyleName("table-empty-inner-label");

      Anchor resetFacetsAnchor = new Anchor(messages.resetFacetsLink());
      resetFacetsAnchor.addStyleName("table-empty-inner-link");
      resetFacetsAnchor.addClickHandler(event -> {
        clearSelectedFacets();
        refresh();
      });

      emptyTablewidget.add(msgBeforeLink);
      emptyTablewidget.add(resetFacetsAnchor);
    } else if (actionable != null) {
      emptyTablewidget.addStyleName("ActionableStyleButtons");

      Label label = new Label();
      label.addStyleName("table-empty-inner-label");
      if (originalFilter.equals(this.getFilter())) {
        label.setText(messages.noItemsToDisplayPreFilters(someOfAObject));
      } else {
        label.setText(messages.noItemsToDisplay(someOfAObject));
      }
      emptyTablewidget.add(label);

      emptyTablewidget.add(
        new ActionableWidgetBuilder<>(actionable).withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
          @Override
          public void onSuccess(Actionable.ActionImpact impact) {
            if (!Actionable.ActionImpact.NONE.equals(impact)) {
              Timer timer = new Timer() {
                @Override
                public void run() {
                  AsyncTableCell.this.refresh();
                }
              };
              timer.schedule(RodaConstants.ACTION_TIMEOUT / 2);
            }
            if (actionableCallback != null) {
              actionableCallback.onSuccess(impact);
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            Timer timer = new Timer() {
              @Override
              public void run() {
                AsyncTableCell.this.refresh();
              }
            };
            timer.schedule(RodaConstants.ACTION_TIMEOUT / 2);
            super.onFailure(caught);
            if (actionableCallback != null) {
              actionableCallback.onFailure(caught);
            }
          }
        }).buildListWithObjects(new ActionableObject<T>(classToReturn)));
    } else {
      Label label = new Label();
      label.addStyleName("table-empty-inner-label");
      if (originalFilter.equals(this.getFilter())) {
        label.setText(messages.noItemsToDisplayPreFilters(someOfAObject));
      } else {
        label.setText(messages.noItemsToDisplay(someOfAObject));
      }
      emptyTablewidget.add(label);
    }
    display.setEmptyTableWidget(emptyTablewidget);
  }

  private void configure(final CellTable<T> display) {
    if (selectable) {
      Column<T, Boolean> selectColumn = new Column<T, Boolean>(new AcessibleCheckboxCell(true, false)) {
        @Override
        public Boolean getValue(T object) {
          return selected.contains(object);
        }
      };

      selectColumn.setFieldUpdater(new FieldUpdater<T, Boolean>() {
        @Override
        public void update(int index, T object, Boolean isSelected) {
          if (isSelected) {
            selected.add(object);
          } else {
            selected.remove(object);
          }

          // update header
          display.redrawHeaders();
          fireOnCheckboxSelectionChanged();
        }
      });

      // pair of (popup, checkbox)
      final Pair<Boolean, Boolean> hoverState = Pair.of(false, false);
      CalloutPopup popup = new CalloutPopup(false, false);
      final int popupTimeoutMs = 750;
      Scheduler.RepeatingCommand updatePopupCommand = () -> {
        if (!resultsPager.isAttached() || (!hoverState.getFirst() && !hoverState.getSecond())) {
          popup.hide();
        } else if (resultsPager.hasNextPage() || resultsPager.hasPreviousPage()) {
          popup.show();
        }
        return false; // do not repeat
      };

      FocusPanel focusPanel = new FocusPanel();
      focusPanel.addMouseOutHandler(event -> {
        hoverState.setFirst(false);
        Scheduler.get().scheduleFixedDelay(updatePopupCommand, popupTimeoutMs);
      });
      focusPanel.addMouseOverHandler(event -> hoverState.setFirst(true));

      // This is not ideal, check issue #1399
      popup.addAttachHandler(attachEvent -> {
        if (attachEvent.isAttached()) {
          selectAllPopupHistoryChangedHandler = History.addValueChangeHandler(historyChangedEvent -> {
            if (selectAllPopupHistoryChangedHandler != null) {
              selectAllPopupHistoryChangedHandler.removeHandler();
            }
            popup.hide();
          });
        }
      });

      FlowPanel popupLayout = new FlowPanel();
      popupLayout.addStyleName("selectAllPopup");

      selectAllRadioButton = new RadioButton("selectedItemsRadio", messages.selectAllPages());
      popupLayout.add(selectAllRadioButton);

      RadioButton buttonPage = new RadioButton("selectedItemsRadio", messages.selectThisPage());
      buttonPage.setValue(true);
      popupLayout.add(buttonPage);

      focusPanel.setWidget(popupLayout);
      popup.setWidget(focusPanel);

      AccessibleHoverableCheckboxCell accessibleHoverableCheckboxCell = new AccessibleHoverableCheckboxCell(true, true);
      accessibleHoverableCheckboxCell.setMouseOverHandlers(element -> {
        // on mouse over
        hoverState.setSecond(true);
        if (resultsPager.hasNextPage() || resultsPager.hasPreviousPage()) {
          popup.setPopupPositionAndShow(
            (offsetWidth, offsetHeight) -> popup.setPopupPosition(element.getAbsoluteLeft() + element.getOffsetWidth(),
              element.getAbsoluteTop() - (int) Math.round(offsetHeight / 2.0 - element.getOffsetHeight() / 2.0)));
        }
      }, element -> {
        // on mouse out
        hoverState.setSecond(false);
        Scheduler.get().scheduleFixedDelay(updatePopupCommand, popupTimeoutMs);
      });

      Header<Boolean> selectHeader = new Header<Boolean>(accessibleHoverableCheckboxCell) {
        @Override
        public Boolean getValue() {
          List<T> visibleItems = getVisibleItems();
          return !visibleItems.isEmpty() && selected.containsAll(visibleItems);
        }
      };

      selectHeader.setUpdater(value -> {
        if (value) {
          selected.addAll(getVisibleItems());
        } else {
          selected.clear();
        }
        redraw();
        fireOnCheckboxSelectionChanged();
      });

      ValueChangeHandler<Boolean> radionButtonValueChangedHandler = event -> {
        selected.addAll(AsyncTableCell.this.getVisibleItems());
        AsyncTableCell.this.redraw();
        AsyncTableCell.this.fireOnCheckboxSelectionChanged();
      };
      selectAllRadioButton.addValueChangeHandler(radionButtonValueChangedHandler);
      buttonPage.addValueChangeHandler(radionButtonValueChangedHandler);

      display.addColumn(selectColumn, selectHeader);
      display.setColumnWidth(selectColumn, "45px");
    }

    configureDisplay(display);
  }

  public Class<T> getClassToReturn() {
    return classToReturn;
  }

  protected abstract void configureDisplay(CellTable<T> display);

  protected int getInitialPageSize() {
    return initialPageSize;
  }

  protected ProvidesKey<T> getKeyProvider() {
    return new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return item.getUUID();
      }
    };
  }

  private CompletableFuture<IndexResult<T>> getData(Sublist sublist, Sorter sorter, List<String> fieldsToReturn) {
    String reason = "Get " + beautifyClassToReturn(getClassToReturn()) + " data";
    Services services = new Services(reason, "get");
    FindRequest findRequest = FindRequest.getBuilder(getFilter(), getJustActive()).withSublist(sublist)
      .withFacets(getFacets()).withExportFacets(false).withSorter(sorter).withFieldsToReturn(fieldsToReturn)
      .withCollapse(getCollapse()).build();
    return services.rodaEntityRestService(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()),
      getClassToReturn());
  }

  private String beautifyClassToReturn(Class<T> classToReturn) {
    // GWT does not support isAssignableFrom
    if (LogEntry.class.getName().equals(classToReturn.getName())) {
      return "audit log";
    } else if (Notification.class.getName().equals(classToReturn.getName())) {
      return "notification";
    } else if (TransferredResource.class.getName().equals(classToReturn.getName())) {
      return "transferred resource";
    } else if (IndexedAIP.class.getName().equals(classToReturn.getName())) {
      return "AIP";
    } else if (IndexedReport.class.getName().equals(classToReturn.getName())) {
      return "report";
    } else {
      return classToReturn.getSimpleName().toLowerCase();
    }
  }

  protected abstract Sorter getSorter(ColumnSortList columnSortList);

  public Sorter getSorter() {
    return dataProvider.getSorter();
  }

  protected int getPageSizePagerIncrement() {
    return pageSizeIncrement;
  }

  protected CellPreviewEvent.Handler<T> getSelectionEventManager() {
    if (selectable) {
      return DefaultSelectionEventManager.<T> createBlacklistManager(0);
    } else {
      return null;
    }
  }

  public SingleSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  public void refresh() {
    selected = new HashSet<>();
    display.setVisibleRangeAndClearData(new Range(0, getInitialPageSize()), true);
    updateEmptyTableWidget();
  }

  public void update() {
    dataProvider.update(fieldsToReturn);
  }

  public void autoUpdate(int periodMillis) {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }

    autoUpdateTimer = new Timer() {

      @Override
      public void run() {
        setAutoUpdateState(AutoUpdateState.AUTO_UPDATE_WORKING);

        dataProvider.update(fieldsToReturn).whenComplete((unused, throwable) -> {
          if (throwable != null) {
            autoUpdateTimer.cancel();
            setAutoUpdateState(AutoUpdateState.AUTO_UPDATE_ERROR);
            LOGGER.error("Could not auto-update table " + listId, throwable);
          } else {
            setAutoUpdateState(AutoUpdateState.AUTO_UPDATE_ON);
          }
        });
      }
    };

    autoUpdateTimerMillis = periodMillis;
    if (this.isAttached()) {
      resumeAutoUpdate();
    }

  }

  @Override
  protected void onDetach() {
    pauseAutoUpdate();
    super.onDetach();
  }

  @Override
  protected void onLoad() {
    if (autoUpdateTimer != null && autoUpdateTimerMillis > 0 && !autoUpdateTimer.isRunning()) {
      resumeAutoUpdate();
    }
    super.onLoad();
  }

  public void redraw() {
    display.redraw();
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
    refresh();
  }

  public Collapse getCollapse() {
    return collapse;
  }

  public boolean isSearchRestricted() {
    return !isAttached() || !isVisible();
  }

  public boolean getJustActive() {
    return justActive;
  }

  public void setJustActive(boolean justActive) {
    this.justActive = justActive;
    refresh();
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
    refresh();
    toggleSidePanel(createAndBindFacets(sidePanel));
  }

  public void set(Filter filter, boolean justActive, Facets facets) {
    this.facets = facets;
    set(filter, justActive);
    toggleSidePanel(createAndBindFacets(sidePanel));
  }

  public void set(Filter filter, boolean justActive) {
    this.filter = filter;
    this.justActive = justActive;
    refresh();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IndexResult<T>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public List<T> getVisibleItems() {
    return display.getVisibleItems();
  }

  protected Sorter createSorter(ColumnSortList columnSortList, Map<Column<T, ?>, List<String>> columnSortingKeyMap) {
    Sorter sorter = new Sorter();
    for (int i = 0; i < columnSortList.size(); i++) {
      ColumnSortInfo columnSortInfo = columnSortList.get(i);

      List<String> sortParameterKeys = columnSortingKeyMap.get(columnSortInfo.getColumn());

      if (sortParameterKeys != null) {
        for (String sortParameterKey : sortParameterKeys) {
          sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
        }
      } else {
        LOGGER.warn("Selecting a sorter that is not mapped");
      }
    }
    return sorter;
  }

  public void nextItemSelection() {
    nextItemSelection(false);
  }

  public void nextItemSelection(boolean nextPageJump) {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (nextPageJump) {
        if (selectedIndex == -1) {
          getSelectionModel().setSelected(getVisibleItems().get(0), true);
        } else {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex + 1), true);
        }
      } else {
        if (selectedIndex < getVisibleItems().size() - 1) {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex + 1), true);
        }
      }
    } else {
      getSelectionModel().setSelected(getVisibleItems().get(0), true);
    }
  }

  public void previousItemSelection() {
    previousItemSelection(false);
  }

  public void previousItemSelection(boolean previousPageJump) {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (previousPageJump) {
        if (selectedIndex == -1) {
          getSelectionModel().setSelected(getVisibleItems().get(getVisibleItems().size() - 1), true);
        } else {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex - 1), true);
        }
      } else {
        if (selectedIndex > 0) {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex - 1), true);
        }
      }
    } else {
      getSelectionModel().setSelected(getVisibleItems().get(0), true);
    }
  }

  public boolean nextPageOnNextFile() {
    boolean nextPage = false;
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      if (getVisibleItems().indexOf(selectedItem) == (resultsPager.getPageSize() - 1) && resultsPager.hasNextPage()) {
        nextPage = true;
      }
    }
    return nextPage;
  }

  public boolean previousPageOnPreviousFile() {
    boolean previousPage = false;
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      if (getVisibleItems().indexOf(selectedItem) == 0 && resultsPager.hasPreviousPage()) {
        previousPage = true;
      }
    }
    return previousPage;
  }

  public void nextPage() {
    resultsPager.nextPage();
  }

  public void previousPage() {
    resultsPager.previousPage();
  }

  public boolean isSelectable() {
    return selectable;
  }

  public SelectedItems<T> getSelected() {
    SelectedItems<T> ret;
    if (isAllSelected()) {
      Filter filterPlusFacets = new Filter(getFilter());
      if (getFacets() != null) {
        for (FacetParameter facetParameter : getFacets().getParameters().values()) {
          if (!facetParameter.getValues().isEmpty()) {
            filterPlusFacets.add(new OneOfManyFilterParameter(facetParameter.getName(), facetParameter.getValues()));
          }
        }
      }

      ret = new SelectedItemsFilter<>(filterPlusFacets, getClassToReturn().getName(), getJustActive());
    } else {
      List<String> ids = new ArrayList<>();

      for (T item : selected) {
        ids.add(item.getUUID());
      }

      ret = new SelectedItemsList<>(ids, getClassToReturn().getName());
    }

    return ret;
  }

  public void setSelected(Set<T> newSelected) {
    selected.clear();
    selected.addAll(newSelected);
    redraw();
    fireOnCheckboxSelectionChanged();
  }

  public void clearSelected() {
    if (selectionModel != null) {
      selected.clear();
      selectionModel.clear();
      redraw();
      fireOnCheckboxSelectionChanged();
    }
  }

  public int getIndexOfVisibleObject(T object) {
    int visibleIndex = getVisibleItems().indexOf(object);
    int visibleStartIndex = display.getVisibleRange().getStart();
    return visibleStartIndex + visibleIndex;
  }

  public ListSelectionState<T> getListSelectionState() {
    ListSelectionState<T> ret = null;
    T selectedObject = getSelectionModel().getSelectedObject();
    if (selectedObject != null) {
      int index = getIndexOfVisibleObject(selectedObject);
      ret = ListSelectionUtils.create(selectedObject, getFilter(), getJustActive(), getFacets(), getSorter(), index,
        getResult().getTotalCount());
    }
    return ret;
  }

  public void addCheckboxSelectionListener(CheckboxSelectionListener<T> checkboxSelectionListener) {
    listeners.add(checkboxSelectionListener);
  }

  public void removeCheckboxSelectionListener(CheckboxSelectionListener<T> listener) {
    listeners.remove(listener);
  }

  public void fireOnCheckboxSelectionChanged() {
    for (CheckboxSelectionListener<T> listener : listeners) {
      listener.onSelectionChange(getSelected());
    }
  }

  public Boolean isAllSelected() {
    return selectable && selectAllRadioButton != null && selectAllRadioButton.getValue()
      && selected.containsAll(getVisibleItems());
  }

  public String getListId() {
    return listId;
  }

  public int getRowCount() {
    return dataProvider.getRowCount();
  }

  public Date getDate() {
    return dataProvider.getDate();
  }

  public List<CheckboxSelectionListener<T>> getListeners() {
    return this.listeners;
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap) {
    addColumn(column, headerHTML, nowrap, false);
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap, boolean alignRight) {
    SafeHtmlHeader header = new SafeHtmlHeader(headerHTML);
    display.addColumn(column, header);

    if (nowrap && alignRight) {
      header.setHeaderStyleNames("nowrap text-align-right");
      column.setCellStyleNames("nowrap text-align-right");
    } else if (alignRight) {
      header.setHeaderStyleNames("text-align-right");
      column.setCellStyleNames("text-align-right");
    } else if (nowrap) {
      header.setHeaderStyleNames("cellTableFadeOut");
      column.setCellStyleNames("cellTableFadeOut");
    }
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap, boolean alignRight,
    double fixedSize) {
    addColumn(column, headerHTML, nowrap, alignRight);
    display.setColumnWidth(column, fixedSize, Unit.EM);
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap, boolean alignRight,
    double fixedSize, Unit unit) {
    addColumn(column, headerHTML, nowrap, alignRight);
    display.setColumnWidth(column, fixedSize, unit);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, false);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap, boolean alignRight) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, alignRight);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap, boolean alignRight,
    double fixedSize) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, alignRight, fixedSize);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap, boolean alignRight, double fixedSize,
    Unit unit) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, alignRight, fixedSize, unit);
  }

  public IndexResult<T> getResult() {
    return result;
  }

  public void setResult(IndexResult<T> result) {
    this.result = result;
  }

  public String translate(String fieldName, String fieldValue) {
    String translation = null;
    if (this.result != null && this.result.getFacetResults() != null) {
      for (FacetFieldResult ffr : this.result.getFacetResults()) {
        if (ffr.getField().equalsIgnoreCase(fieldName)) {
          if (ffr.getValues() != null) {
            for (FacetValue fv : ffr.getValues()) {
              if (fv.getValue().equalsIgnoreCase(fieldValue)) {
                translation = fv.getLabel();
                break;
              }
            }
          }
        }
        if (translation != null) {
          break;
        }
      }
    }
    if (translation == null) {
      translation = fieldValue;
    }
    return translation;
  }

  public boolean hasElementsSelected() {
    SelectedItems<T> selected = getSelected();

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<T> list = (SelectedItemsList<T>) selected;
      return !list.getIds().isEmpty();
    }

    return true;
  }

  public ActionableObject<T> getActionableObject() {
    if (isAllSelected()) {
      return new ActionableObject<>(getSelected());
    } else if (selected.size() == 1) {
      return new ActionableObject<>(selected.iterator().next());
    } else if (selected.size() > 1) {
      return new ActionableObject<>(getSelected());
    } else {
      return new ActionableObject<>(classToReturn);
    }
  }

  private boolean createAndBindFacets(FlowPanel facetsPanel) {
    facetsPanel.clear();

    if (facetsValueChangedHandlerRegistration != null) {
      facetsValueChangedHandlerRegistration.removeHandler();
    }

    Map<String, FlowPanel> facetPanels = createInnerFacetPanels(facetsPanel);
    facetsValueChangedHandlerRegistration = addValueChangeHandler(listValueChangedEvent -> {
      List<FacetFieldResult> facetResults = listValueChangedEvent.getValue().getFacetResults();

      boolean allFacetsAreEmpty = true;
      for (FacetFieldResult facetResult : facetResults) {
        final String facetField = facetResult.getField();
        FlowPanel facetPanel = facetPanels.get(facetField);
        if (facetPanel != null) {
          facetPanel.clear();
          if (facetResult.getTotalCount() == 0) {
            facetPanel.getParent().addStyleName("facet-empty");
          } else {
            allFacetsAreEmpty = false;
            facetPanel.getParent().removeStyleName("facet-empty");
          }

          for (FacetValue facetValue : facetResult.getValues()) {
            final String value = facetValue.getValue();
            final String label = facetValue.getLabel();
            long count = facetValue.getCount();
            boolean facetIsSelected = facetResult.getSelectedValues().contains(value);
            StringBuilder checkboxLabel = new StringBuilder();
            checkboxLabel.append(label);
            if (count > 0 || facetResult.getSelectedValues().isEmpty() || facetIsSelected) {
              checkboxLabel.append(" (").append(count).append(")");
            }

            CheckBox facetValuePanel = new CheckBox(checkboxLabel.toString());
            facetValuePanel.setTitle(checkboxLabel.toString());
            facetValuePanel.addStyleName("sidebar-facet-label");
            facetValuePanel.addStyleName("fade-out");

            boolean enabled = count > 0 || !facetResult.getSelectedValues().isEmpty();
            facetValuePanel.setEnabled(enabled);

            facetPanel.add(facetValuePanel);
            facetValuePanel.setValue(facetIsSelected);

            facetValuePanel.addValueChangeHandler(facetValueChangedEvent -> {
              FacetParameter selectedFacetParameter = getFacets().getParameters().get(facetField);

              if (selectedFacetParameter != null) {
                if (facetValueChangedEvent.getValue()) {
                  selectedFacetParameter.getValues().add(value);
                } else {
                  selectedFacetParameter.getValues().remove(value);
                }
              } else {
                LOGGER.warn("Haven't found the facet parameter: " + facetField);
              }
              refresh();
            });
          }
        } else {
          LOGGER.warn("Got a facet but haven't got a panel for it: " + facetField);
        }
      }

      facetsPanel.setVisible(!allFacetsAreEmpty);
    });
    return !facetPanels.isEmpty();
  }

  private Map<String, FlowPanel> createInnerFacetPanels(final FlowPanel facetsPanel) {
    Map<String, FlowPanel> innerFacetPanels = new HashMap<>();
    for (FacetParameter facetParameter : getFacets().getParameters().values()) {
      FlowPanel facetAndTitle = new FlowPanel();
      facetAndTitle.addStyleName("sidebar-facet-panel");

      String title = ConfigurationManager.getTranslationWithDefault(facetParameter.getName(),
        RodaConstants.I18N_UI_FACETS_PREFIX, getClassToReturn().getSimpleName(), facetParameter.getName());

      Label titleLabel = new Label(title);
      titleLabel.addStyleName("h5");

      FlowPanel facetPanel = new FlowPanel();
      facetPanel.addStyleName("facet-input-panel");

      facetAndTitle.add(titleLabel);
      facetAndTitle.add(facetPanel);
      facetsPanel.add(facetAndTitle);

      innerFacetPanels.put(facetParameter.getName(), facetPanel);
    }

    return innerFacetPanels;
  }

  private boolean hasSelectedFacets() {
    boolean hasSelectedFacets = false;
    if (getFacets() != null) {
      for (FacetParameter facetParameter : getFacets().getParameters().values()) {
        if (!facetParameter.getValues().isEmpty()) {
          hasSelectedFacets = true;
          break;
        }
      }
    }
    return hasSelectedFacets;
  }

  private void clearSelectedFacets() {
    for (Map.Entry<String, FacetParameter> entry : getFacets().getParameters().entrySet()) {
      entry.getValue().getValues().clear();
    }
  }

  public AutoUpdateState getAutoUpdateState() {
    return autoUpdateState;
  }

  public void setAutoUpdateState(AutoUpdateState autoUpdateState) {
    this.autoUpdateState = autoUpdateState;
    ClickHandler clickHandler = null;

    if (autoUpdateState.equals(AutoUpdateState.AUTO_UPDATE_OFF)) {
      autoUpdateSignal.setHTML("");
    } else if (autoUpdateState.equals(AutoUpdateState.AUTO_UPDATE_ON)) {
      autoUpdateSignal.setHTML(
        "<i class='fas fa-circle-notch fa-spin' title='" + messages.tableUpdateOn() + "' style='cursor: pointer'></i>");

      clickHandler = event -> pauseAutoUpdate();
    } else if (autoUpdateState.equals(AutoUpdateState.AUTO_UPDATE_WORKING)) {
      autoUpdateSignal.setHTML(
        "<i class='fas fa-sync fa-spin' title='" + messages.tableUpdating() + "' style='cursor: pointer'></i>");
    } else if (autoUpdateState.equals(AutoUpdateState.AUTO_UPDATE_PAUSED)) {
      autoUpdateSignal.setHTML("<i class='fas fa-pause-circle' title='" + messages.tableUpdatePause()
        + "' style='cursor: pointer; color: #222'></i>");

      clickHandler = event -> resumeAutoUpdate();
    } else if (autoUpdateState.equals(AutoUpdateState.AUTO_UPDATE_ERROR)) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.appendHtmlConstant("<span title='" + messages.tableUpdateErrorConnection() + "' style='cursor: pointer'>");
      b.appendHtmlConstant("<i class='fas fa-plug ' style='color: #222'></i>");
      b.appendHtmlConstant(
        "<i class='fas fa-times' style='font-size: 0.5em; color: Tomato; margin-left: -2px; vertical-align: text-bottom;'></i>");
      b.appendHtmlConstant("</span>");
      autoUpdateSignal.setHTML(b.toSafeHtml());

      clickHandler = event -> resumeAutoUpdate();
    }

    if (autoUpdateHandler != null) {
      autoUpdateHandler.removeHandler();
    }

    if (clickHandler != null) {
      autoUpdateHandler = autoUpdatePanel.addClickHandler(clickHandler);
    }
  }

  public void addAutoUpdateControlListener() {
    listeners.add(new CheckboxSelectionListener<T>() {
      @Override
      public void onSelectionChange(SelectedItems<T> selected) {
        if (ClientSelectedItemsUtils.isEmpty(selected)) {
          resumeAutoUpdate();
        } else {
          pauseAutoUpdate();
        }
      }
    });
  }

  public void pauseAutoUpdate() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
      setAutoUpdateState(AutoUpdateState.AUTO_UPDATE_PAUSED);
    }
  }

  public void resumeAutoUpdate() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.scheduleRepeating(autoUpdateTimerMillis);
      setAutoUpdateState(AutoUpdateState.AUTO_UPDATE_ON);
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!isSearchRestricted()) {
      originalFilter = this.getFilter();
      refresh();
    }
  }

  enum AutoUpdateState {
    AUTO_UPDATE_OFF, AUTO_UPDATE_ON, AUTO_UPDATE_ERROR, AUTO_UPDATE_PAUSED, AUTO_UPDATE_WORKING
  }

  // LISTENER
  @FunctionalInterface
  public interface CheckboxSelectionListener<T extends IsIndexed> {
    public void onSelectionChange(SelectedItems<T> selected);
  }
}
