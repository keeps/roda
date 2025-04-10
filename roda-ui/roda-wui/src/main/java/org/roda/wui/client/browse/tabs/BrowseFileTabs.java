package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.IndexedFilePreview;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseFileResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.services.Services;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseFileTabs extends Tabs {
  public void init(Viewers viewers, IndexedFile file, BrowseFileResponse browseFileResponse, Services services) {
    IndexedAIP aip = browseFileResponse.getIndexedAIP();
    IndexedRepresentation representation = browseFileResponse.getIndexedRepresentation();

    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // PREVIEW
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.viewTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new IndexedFilePreview(viewers, file, file.isAvailable(), justActive,
          browseFileResponse.getIndexedAIP().getPermissions(), () -> {
          });
      }
    });

    // PRESERVATION EVENTS
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEvents()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        Filter eventFilter = new Filter(new AllFilterParameter());
        eventFilter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_FILE_UUID, file.getUUID()));
        return new SearchWrapper(false).createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(),
          new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseFile_preservationEvents")
            .withFilter(eventFilter).withSummary(messages.searchResults()).bindOpener()));
      }
    });

    // RISKS
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.risksTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        SearchWrapper riskIncidences = new SearchWrapper(false);
        riskIncidences.createListAndSearchPanel(new ListBuilder<>(() -> new RiskIncidenceList(),
          new AsyncTableCellOptions<>(RiskIncidence.class, "BrowseFile_riskIncidences")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, file.getId())))
            .withJustActive(justActive).bindOpener()));
        return riskIncidences;
      }
    });

    // Details
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DetailsTab(aip, representation, file);
      }
    });
  }
}