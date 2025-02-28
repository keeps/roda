package org.roda.wui.client.browse.tabs;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.EditPermissionsTab;
import org.roda.wui.client.common.actions.AIPToolbarActions;
import org.roda.wui.client.common.actions.PreservationEventActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationTab;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseAIPTabs extends Tabs {
  public void init(IndexedAIP aip, BrowseAIPResponse browseAIPResponse,
    DescriptiveMetadataInfos descriptiveMetadataInfos) {
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());
    // Descriptive metadata
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        AIPDescriptiveMetadataTabs aipDescriptiveMetadataTabs = new AIPDescriptiveMetadataTabs();
        aipDescriptiveMetadataTabs.init(aip, descriptiveMetadataInfos);
        aipDescriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
        return aipDescriptiveMetadataTabs;
      }
    });

    // Preservation events
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEventsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        Filter eventFilter = new Filter(new AllFilterParameter());
        eventFilter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
        return new SearchWrapper(false)
          .createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(),
            new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseAIP_preservationEvents")
              .withFilter(eventFilter).withSummary(messages.searchResults()).bindOpener()
              .withActionable(PreservationEventActions.get(aip.getId(), null, null))));
      }
    });

    // Logs
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.auditLogsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        SearchWrapper auditLogs = new SearchWrapper(false);
        auditLogs.createListAndSearchPanel(new ListBuilder<>(() -> new LogEntryList(),
          new AsyncTableCellOptions<>(LogEntry.class, "BrowseAIP_auditLogs")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aip.getId())))
            .withJustActive(justActive).bindOpener()));
        return auditLogs;
      }
    });

    // Risk incidences
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.risksTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        SearchWrapper riskIncidences = new SearchWrapper(false);
        riskIncidences.createListAndSearchPanel(new ListBuilder<>(() -> new RiskIncidenceList(),
          new AsyncTableCellOptions<>(RiskIncidence.class, "BrowseAIP_riskIncidences")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aip.getId())))
            .withJustActive(justActive).bindOpener()));
        return riskIncidences;
      }
    });

    // Disposal
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DisposalPolicyAssociationTab(browseAIPResponse);
      }
    });

    // Permissions
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        AIPToolbarActions aipToolbarActions = AIPToolbarActions.get(aip.getId(), aip.getState(), aip.getPermissions());
        return new EditPermissionsTab(new ActionableWidgetBuilder<>(aipToolbarActions).buildGroupedListWithObjects(
          new ActionableObject<>(aip), List.of(AIPToolbarActions.AIPAction.UPDATE_PERMISSIONS),
          List.of(AIPToolbarActions.AIPAction.UPDATE_PERMISSIONS)), IndexedAIP.class.getName(), aip);
      }
    });

  }
}