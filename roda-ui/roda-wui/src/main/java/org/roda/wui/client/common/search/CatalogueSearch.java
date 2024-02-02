/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CatalogueSearch extends Composite {
  private ClientLogger logger = new ClientLogger(getClass().getName());

  interface MyUiBinder extends UiBinder<Widget, CatalogueSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final List<Class<? extends IsIndexed>> searchableClasses = Arrays.asList(IndexedAIP.class,
    IndexedRepresentation.class, IndexedFile.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  private Supplier<Map<String, Filter>> createClassFilters() {
    Map<String, Filter> classFilters = new HashMap<>();
    for (Class<? extends IsIndexed> searchableClass : searchableClasses) {
      classFilters.put(searchableClass.getSimpleName(), new Filter(new AllFilterParameter()));
    }
    return () -> classFilters;
  }

  public CatalogueSearch(List<String> filterHistoryTokens, boolean justActive, String itemsListId,
    String representationsListId, String filesListId, Permissions permissions, boolean startHidden,
    boolean redirectOnSingleResult) {

    // get classes to show and preFilters to use
    Map<String, Filter> classFilters;
    if (!filterHistoryTokens.isEmpty()) {
      classFilters = parseFilters(filterHistoryTokens).orElseGet(() -> createClassFilters().get());
      redirectOnSingleResult = calculateRedirectOnSingleResult(filterHistoryTokens, classFilters,
        redirectOnSingleResult);
    } else {
      classFilters = new HashMap<>();
      for (Class<? extends IsIndexed> searchableClass : searchableClasses) {
        classFilters.put(searchableClass.getSimpleName(), new Filter(new AllFilterParameter()));
      }
    }

    searchWrapper = new SearchWrapper(true);

    String preselectedDropdownValue = null;
    for (Class<? extends IsIndexed> searchableClass : searchableClasses) {
      if (classFilters.containsKey(searchableClass.getSimpleName())) {

        Filter filter = classFilters.get(searchableClass.getSimpleName());
        ListBuilder<?> listBuilder = null;
        if (searchableClass.equals(IndexedAIP.class)
          && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {

          listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
            new AsyncTableCellOptions<>(IndexedAIP.class, itemsListId)
              .withActionable(AipActions.getWithoutNoAipActions(null, AIPState.ACTIVE, permissions))
              .withRedirectOnSingleResult(redirectOnSingleResult).withJustActive(justActive).bindOpener()
              .withFilter(filter).withStartHidden(startHidden));
        } else if (searchableClass.equals(IndexedRepresentation.class)
          && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {

          listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
            new AsyncTableCellOptions<>(IndexedRepresentation.class, representationsListId)
              .withActionable(RepresentationActions.getWithoutNoRepresentationActions(null, null))
              .withRedirectOnSingleResult(redirectOnSingleResult).withJustActive(justActive).bindOpener()
              .withFilter(filter).withStartHidden(startHidden));
        } else if (searchableClass.equals(IndexedFile.class)
          && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_FILE)) {

          listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
            new AsyncTableCellOptions<>(IndexedFile.class, filesListId)
              .withActionable(FileActions.getWithoutNoFileActions(null, null, null, null))
              .withRedirectOnSingleResult(redirectOnSingleResult).withJustActive(justActive).bindOpener()
              .withFilter(filter).withStartHidden(startHidden));
        }

        if (listBuilder != null) {
          if (preselectedDropdownValue == null) {
            preselectedDropdownValue = searchableClass.getSimpleName();
          }
          searchWrapper.createListAndSearchPanel(listBuilder, true);
        }
      }
    }

    if (preselectedDropdownValue != null) {
      searchWrapper.changeDropdownSelectedValue(preselectedDropdownValue);
    }

    initWidget(uiBinder.createAndBindUi(this));
  }

  public CatalogueSearch(boolean justActive, String itemsListId, String representationsListId, String filesListId,
    Permissions permissions, boolean startHidden, boolean redirectOnSingleResult) {
    this(Collections.emptyList(), justActive, itemsListId, representationsListId, filesListId, permissions, startHidden,
      redirectOnSingleResult);
  }

  public void refresh() {
    searchWrapper.refreshCurrentList();
  }

  private boolean calculateRedirectOnSingleResult(List<String> historyTokens, Map<String, Filter> classFilters,
    boolean redirectOnSingleResult) {
    if (historyTokens.get(0).startsWith("$")) {
      return false;
    }

    return redirectOnSingleResult && classFilters.keySet().size() <= 1;
  }

  private Optional<Map<String, Filter>> parseFilters(List<String> historyTokens) {
    if (historyTokens.get(0).startsWith("$")) {
      return handleSavedSearch(historyTokens);
    } else if (historyTokens.get(0).startsWith("@")) {
      return handlePreFilterSearch(historyTokens);
    } else {
      logger.error("setFilter can not handle tokens: " + historyTokens);
    }

    return Optional.empty();
  }

  private Optional<Map<String, Filter>> handleSavedSearch(List<String> historyTokens) {
    Map<String, Filter> classFilters = new HashMap<>();
    if (historyTokens.size() == 2) {
      String jsonValue = JavascriptUtils.decodeBase64(historyTokens.get(1));
      SavedSearchMapper mapper = GWT.create(SavedSearchMapper.class);
      RODASavedSearch savedSearch = mapper.read(jsonValue);
      classFilters.put(savedSearch.getSearchClassName(), savedSearch.getFilter());
      return Optional.of(classFilters);
    } else {
      return Optional.empty();
    }
  }

  private Optional<Map<String, Filter>> handlePreFilterSearch(List<String> historyTokens) {
    // classSimpleName -> filter
    Map<String, Filter> classFilters = new HashMap<>();
    ListIterator<String> tokens = historyTokens.listIterator();
    while (tokens.hasNext()) {
      List<String> classes = new ArrayList<>(Arrays.asList(tokens.next().split("@")));

      // should start with @, so remove the empty string
      if (classes.size() > 1 && classes.get(0).isEmpty()) {
        classes.remove(0);

        // get filter
        if (tokens.hasNext()) {
          List<String> filterTokens = new ArrayList<>();

          String possibleOperand = tokens.next();
          if (RodaConstants.OPERATOR_AND.equals(possibleOperand) || RodaConstants.OPERATOR_OR.equals(possibleOperand)) {
            filterTokens.add(possibleOperand);
          } else {
            tokens.previous();
          }

          while (tokens.hasNext()) {
            String field = tokens.next();
            if (field.startsWith("@")) {
              tokens.previous();
              break;
            }

            // add key/value pair
            filterTokens.add(field);
            if (tokens.hasNext()) {
              // filter value
              filterTokens.add(tokens.next());
            }
          }

          Filter filter = SearchFilters.createFilterFromHistoryTokens(filterTokens);
          if (!filter.equals(new Filter(new AllFilterParameter()))) {
            for (String aClass : classes) {
              classFilters.put(aClass, filter);
            }
          } else {
            logger.error("Could not parse filter (" + StringUtils.join(filterTokens, "/") + ") for classes "
              + StringUtils.join(classes, ", ") + ". List of tokens: " + StringUtils.join(historyTokens, "/"));
          }
        }
      } else {
        logger.error("setFilter can not handle tokens: " + historyTokens);
        break;
      }
    }

    return Optional.of(classFilters);
  }
}