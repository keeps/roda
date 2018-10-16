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
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

public class PreservationEventsSearch extends SimplePanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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

    SearchWrapper searchWrapper = new SearchWrapper(false)
      .createListAndSearchPanel(
      new ListBuilder<>(() -> new PreservationEventList(),
        new AsyncTableCellOptions<>(IndexedPreservationEvent.class, eventsListId).withFilter(filter)
          .withSummary(messages.searchResults()).bindOpener()
          .withActionable(PreservationEventActions.get(aipId, representationUUID, fileUUID))));

    setWidget(searchWrapper);
  }
}
