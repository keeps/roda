/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class CatalogueSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, CatalogueSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public CatalogueSearch(boolean justActive, String itemsListId, String representationsListId, String filesListId,
    Permissions permissions, boolean startHidden) {

    // prepare lists
    ListBuilder<IndexedAIP> aipListBuilder = new ListBuilder<>(() -> new AIPList(),
      new AsyncTableCellOptions<>(IndexedAIP.class, itemsListId).withJustActive(justActive).bindOpener()
        .withStartHidden(startHidden)
        .withActionable(AipActions.getWithoutNoAipActions(null, AIPState.ACTIVE, permissions)));

    // add lists to search
    searchWrapper = new SearchWrapper(true, IndexedAIP.class.getSimpleName()).createListAndSearchPanel(aipListBuilder);

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      ListBuilder<IndexedRepresentation> representationListBuilder = new ListBuilder<>(() -> new RepresentationList(),
        new AsyncTableCellOptions<>(IndexedRepresentation.class, representationsListId).withJustActive(justActive)
          .bindOpener().withStartHidden(startHidden)
          .withActionable(RepresentationActions.getWithoutNoRepresentationActions(null, null)));
      searchWrapper.createListAndSearchPanel(representationListBuilder);
    }

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_FILE)) {
      ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new SearchFileList(true),
        new AsyncTableCellOptions<>(IndexedFile.class, filesListId).withJustActive(justActive).bindOpener()
          .withStartHidden(startHidden).withActionable(FileActions.getWithoutNoFileActions(null, null, null, null)));
      searchWrapper.createListAndSearchPanel(fileListBuilder);
    }

    initWidget(uiBinder.createAndBindUi(this));
  }

  public void refresh() {
    searchWrapper.refreshCurrentList();
  }

  public void setFilters(List<String> historyTokens) {
    if (!historyTokens.isEmpty()) {
      Filter filter = SearchFilters.createFilterFromHistoryTokens(ListUtils.tail(historyTokens));

      String classSimpleName = historyTokens.get(0);
      if (IndexedRepresentation.class.getSimpleName().equals(classSimpleName)) {
        searchWrapper.setFilter(IndexedRepresentation.class, filter);
        searchWrapper.changeDropdownSelectedValue(classSimpleName);
      } else if (IndexedFile.class.getSimpleName().equals(classSimpleName)) {
        searchWrapper.setFilter(IndexedFile.class, filter);
        searchWrapper.changeDropdownSelectedValue(classSimpleName);
      } else if (IndexedAIP.class.getSimpleName().equals(classSimpleName)) {
        searchWrapper.setFilter(IndexedAIP.class, filter);
        searchWrapper.changeDropdownSelectedValue(classSimpleName);
      } else {
        GWT.log("setFilter can not handle tokens: " + historyTokens);
      }
    }
  }
}
