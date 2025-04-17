package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.actions.FileSearchWrapperActions;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseRepresentationTabs extends Tabs {
  public void init(BrowseRepresentationResponse browseRepresentationResponse) {
    IndexedAIP aip = browseRepresentationResponse.getIndexedAIP();
    IndexedRepresentation representation = browseRepresentationResponse.getIndexedRepresentation();

    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // Files
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.filesTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        Filter filesFilter = new Filter(
          new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID,
            browseRepresentationResponse.getIndexedRepresentation().getUUID()),
          new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));

        String summary = messages.representationListOfFiles();

        ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
          new AsyncTableCellOptions<>(IndexedFile.class, "BrowseRepresentation_files").withFilter(filesFilter)
            .withJustActive(justActive).withSummary(summary).bindOpener()
            .withActionable(FileSearchWrapperActions.get(aip.getId(), representation.getId(), aip.getState(), aip.getPermissions())));

        return new SearchWrapper(false).createListAndSearchPanel(fileListBuilder);
      }
    });

    // Descriptive metadata
    if (!browseRepresentationResponse.getDescriptiveMetadataInfos().getDescriptiveMetadataInfoList().isEmpty()) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          RepresentationDescriptiveMetadataTabs descriptiveMetadataTabs = new RepresentationDescriptiveMetadataTabs();
          descriptiveMetadataTabs.init(aip, representation, browseRepresentationResponse.getDescriptiveMetadataInfos());
          descriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
          return descriptiveMetadataTabs;
        }
      });
    }

    // Preservation events
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEventsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        Filter eventFilter = new Filter(new AllFilterParameter());
        eventFilter.add(
          new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representation.getUUID()));
        return new SearchWrapper(false).createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(),
          new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseRepresentation_preservationEvents")
            .withFilter(eventFilter).withSummary(messages.searchResults()).bindOpener()));
      }
    });

    // Risk incidences
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.risksTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        SearchWrapper riskIncidences = new SearchWrapper(false);
        riskIncidences.createListAndSearchPanel(new ListBuilder<>(() -> new RiskIncidenceList(),
          new AsyncTableCellOptions<>(RiskIncidence.class, "BrowseRepresentation_riskIncidences")
            .withFilter(new Filter(
              new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representation.getId())))
            .withJustActive(justActive).bindOpener()));
        return riskIncidences;
      }
    });

    // Details
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DetailsTab(browseRepresentationResponse);
      }
    });
  }
}
