/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.EntitySearch;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.management.ShowNotification;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

public class NotificationSearch extends Composite implements EntitySearch {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, NotificationSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterNotifications;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  NotificationList notificationsSearchResultPanel;
  AdvancedSearchFieldsPanel notificationsSearchAdvancedFieldsPanel;
  String notificationsListId;

  public NotificationSearch(String notificationsListId) {
    this.notificationsListId = notificationsListId;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    notificationsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_NOTIFICATIONS);
    notificationsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    defaultFilters(Filter.ALL);

    searchPanel = new SearchPanel(filterNotifications, RodaConstants.NOTIFICATION_SEARCH, true,
      messages.searchPlaceHolder(), false, true, false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxNotifications());
    searchPanel.addDropdownItem(messages.searchListBoxNotifications(), RodaConstants.SEARCH_NOTIFICATIONS);
    search();

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        search();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        notificationsSearchAdvancedFieldsPanel.addSearchFieldPanel();
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (notificationsSearchResultPanel == null) {
      createNotificationsSearchResultPanel();
    }

    searchPanel.setVariables(filterNotifications, RodaConstants.NOTIFICATION_SEARCH, true,
      notificationsSearchResultPanel, notificationsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(notificationsSearchResultPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(notificationsSearchResultPanel);
  }

  private void createNotificationsSearchResultPanel() {
    notificationsSearchResultPanel = new NotificationList(notificationsListId, filterNotifications,
      messages.searchResults(), false);
    ListSelectionUtils.bindBrowseOpener(notificationsSearchResultPanel);

    notificationsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Notification selected = notificationsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (selected != null) {
          LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
          HistoryUtils.newHistory(ShowNotification.RESOLVER, selected.getId());
        }
      }
    });
  }

  @Override
  public void refresh() {
    if (notificationsSearchResultPanel != null && notificationsSearchResultPanel.hasElementsSelected()) {
      notificationsSearchResultPanel.refresh();
    }
  }

  public void search() {
    showSearchAdvancedFieldsPanel();
    searchPanel.doSearch();
  }

  public void defaultFilters(Filter filter) {
    filterNotifications = new Filter(filter);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  @Override
  public BasicAsyncTableCell<?> getList() {
    return notificationsSearchResultPanel;
  }

  @Override
  public void setFilter(Filter filter) {
    notificationsSearchResultPanel.setFilter(filter);
  }
}
