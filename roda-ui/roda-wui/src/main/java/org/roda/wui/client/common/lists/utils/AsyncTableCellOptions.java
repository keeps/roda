package org.roda.wui.client.common.lists.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.AbstractHasData.RedrawEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;

public class AsyncTableCellOptions<T extends IsIndexed> {
  private final Class<T> classToReturn;
  private final String listId;
  private Filter filter;
  private boolean justActive;
  private Facets facets;
  private String summary;
  private List<String> fieldsToReturn;
  private int initialPageSize;
  private int pageSizeIncrement;
  private Actionable<T> actionable;
  private AsyncCallback<Actionable.ActionImpact> actionableCallback;
  private boolean bindOpener;
  private List<AsyncTableCell.CheckboxSelectionListener<T>> checkboxSelectionListeners;
  private List<ValueChangeHandler<IndexResult<T>>> indexResultValueChangeHandlers;
  private List<SelectionChangeEvent.Handler> selectionChangeHandlers;
  private List<RedrawEvent.Handler> redrawEventHandlers;
  private List<String> extraStyleNames;
  private Integer autoUpdate;
  private boolean csvDownloadButtonVisibility;
  private boolean startHidden;
  private String searchPlaceholder;
  private boolean forceSelectable;

  public AsyncTableCellOptions(Class<T> classToReturn, String listId) {
    this.classToReturn = classToReturn;
    this.listId = listId;

    // set defaults
    actionable = null;
    actionableCallback = null;
    filter = SearchFilters.allFilter();
    justActive = false;
    facets = ConfigurationManager.FacetFactory.getFacets(listId);
    summary = AsyncTableCell.messages.searchResults();
    fieldsToReturn = Collections.emptyList();
    initialPageSize = AsyncTableCell.DEFAULT_INITIAL_PAGE_SIZE;
    pageSizeIncrement = AsyncTableCell.DEFAULT_PAGE_SIZE_INCREMENT;
    checkboxSelectionListeners = new ArrayList<>();
    indexResultValueChangeHandlers = new ArrayList<>();
    extraStyleNames = new ArrayList<>();
    selectionChangeHandlers = new ArrayList<>();
    redrawEventHandlers = new ArrayList<>();
    autoUpdate = null;
    csvDownloadButtonVisibility = true;
    startHidden = false;
    searchPlaceholder = null;
    forceSelectable = false;
  }

  public Class<T> getClassToReturn() {
    return classToReturn;
  }

  public AsyncTableCellOptions<T> withActionable(Actionable<T> actionable) {
    this.actionable = actionable;
    return this;
  }

  public AsyncTableCellOptions<T> withFilter(Filter filter) {
    this.filter = filter;
    return this;
  }

  public AsyncTableCellOptions<T> withJustActive(boolean justActive) {

    this.justActive = justActive;
    return this;
  }

  public AsyncTableCellOptions<T> withFacets(Facets facets) {
    this.facets = facets;
    return this;
  }

  public AsyncTableCellOptions<T> withSummary(String summary) {
    this.summary = summary;
    return this;
  }

  public AsyncTableCellOptions<T> withFieldsToReturn(List<String> fieldsToReturn) {
    this.fieldsToReturn = fieldsToReturn;
    return this;
  }

  public AsyncTableCellOptions<T> withInitialPageSize(int initialPageSize) {
    this.initialPageSize = initialPageSize;
    return this;
  }

  public AsyncTableCellOptions<T> withPageSizeIncrement(int pageSizeIncrement) {
    this.pageSizeIncrement = pageSizeIncrement;
    return this;
  }

  public AsyncTableCellOptions<T> withAutoUpdate(Integer autoUpdate) {
    this.autoUpdate = autoUpdate;
    return this;
  }

  // move this to the searchWrapper and/or use actionable
  @Deprecated
  public AsyncTableCellOptions<T> withCsvDownloadButtonVisibility(boolean csvDownloadButtonVisibility) {
    this.csvDownloadButtonVisibility = csvDownloadButtonVisibility;
    return this;
  }

  public AsyncTableCellOptions<T> bindOpener() {
    this.bindOpener = true;
    return this;
  }

  public AsyncTableCellOptions<T> withStartHidden(boolean startHidden) {
    this.startHidden = startHidden;
    return this;
  }

  public AsyncTableCellOptions<T> addCheckboxSelectionListener(
    AsyncTableCell.CheckboxSelectionListener<T> checkboxSelectionListener) {
    checkboxSelectionListeners.add(checkboxSelectionListener);
    return this;
  }

  public AsyncTableCellOptions<T> addValueChangedHandler(ValueChangeHandler<IndexResult<T>> handler) {
    indexResultValueChangeHandlers.add(handler);
    return this;
  }

  public AsyncTableCellOptions<T> addStyleName(String styleName) {
    extraStyleNames.add(styleName);
    return this;
  }

  public AsyncTableCellOptions<T> addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
    selectionChangeHandlers.add(handler);
    return this;
  }

  public AsyncTableCellOptions<T> addRedrawHandler(RedrawEvent.Handler handler) {
    redrawEventHandlers.add(handler);
    return this;
  }

  public String getListId() {
    return listId;
  }

  public Filter getFilter() {
    return filter;
  }

  public boolean isJustActive() {
    return justActive;
  }

  public Facets getFacets() {
    return facets;
  }

  public String getSummary() {
    return summary;
  }

  public List<String> getFieldsToReturn() {
    return fieldsToReturn;
  }

  public int getInitialPageSize() {
    return initialPageSize;
  }

  public int getPageSizeIncrement() {
    return pageSizeIncrement;
  }

  public Actionable<T> getActionable() {
    return actionable;
  }

  public boolean isBindOpener() {
    return bindOpener;
  }

  public List<AsyncTableCell.CheckboxSelectionListener<T>> getCheckboxSelectionListeners() {
    return checkboxSelectionListeners;
  }

  public List<ValueChangeHandler<IndexResult<T>>> getIndexResultValueChangeHandlers() {
    return indexResultValueChangeHandlers;
  }

  public List<SelectionChangeEvent.Handler> getSelectionChangeHandlers() {
    return selectionChangeHandlers;
  }

  public List<RedrawEvent.Handler> getRedrawEventHandlers() {
    return redrawEventHandlers;
  }

  public List<String> getExtraStyleNames() {
    return extraStyleNames;
  }

  public Integer getAutoUpdate() {
    return autoUpdate;
  }

  public boolean isCsvDownloadButtonVisibility() {
    return csvDownloadButtonVisibility;
  }

  public boolean isStartHidden() {
    return startHidden;
  }

  public String getSearchPlaceholder() {
    return searchPlaceholder;
  }

  public AsyncTableCellOptions<T> withSearchPlaceholder(String searchPlaceholder) {
    this.searchPlaceholder = searchPlaceholder;
    return this;
  }

  public AsyncCallback<Actionable.ActionImpact> getActionableCallback() {
    return actionableCallback;
  }

  public AsyncTableCellOptions<T> withActionableCallback(AsyncCallback<Actionable.ActionImpact> actionableCallback) {
    this.actionableCallback = actionableCallback;
    return this;
  }

  public boolean getForceSelectable() {
    return forceSelectable;
  }

  public AsyncTableCellOptions<T> withForceSelectable(boolean forceSelectable) {
    this.forceSelectable = forceSelectable;
    return this;
  }
}
