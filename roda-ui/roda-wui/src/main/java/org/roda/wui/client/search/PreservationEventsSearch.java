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
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.common.actions.PreservationEventActions;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class PreservationEventsSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, PreservationEventsSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public PreservationEventsSearch(String eventsListId, String aipId, String representationUUID, String fileUUID) {
    Filter filter = new Filter();
    if (aipId != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId));
    }
    if (representationUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representationUUID));
    }
    if (fileUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_FILE_UUID, fileUUID));
    }

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(
      new ListBuilder<>(() -> new PreservationEventList(),
        new AsyncTableCellOptions<>(IndexedPreservationEvent.class, eventsListId).withFilter(filter)
          .withSummary(messages.searchResults()).bindOpener()),
      PreservationEventActions.get(aipId, representationUUID, fileUUID), messages.searchPlaceHolder());

    initWidget(uiBinder.createAndBindUi(this));

    // TODO tmp
    // searchPanel.setDropdownLabel(messages.searchListBoxPreservationEvents());
    // searchPanel.addDropdownItem(messages.searchListBoxPreservationEvents(),
    // RodaConstants.SEARCH_PRESERVATION_EVENTS);
  }
}
