/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.EditPermissionsTab;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationTab;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseAIPTabs extends Tabs {
  public void init(BrowseAIPResponse browseAIPResponse, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    IndexedAIP aip = browseAIPResponse.getIndexedAIP();

    boolean justActive = AIPState.ACTIVE.equals(aip.getState());
    // Descriptive metadata
    // Check if user has permissions to see the descriptive metadata
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA)) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          AIPDescriptiveMetadataTabs aipDescriptiveMetadataTabs = new AIPDescriptiveMetadataTabs();
          aipDescriptiveMetadataTabs.init(aip, browseAIPResponse.getDescriptiveMetadataInfos());
          aipDescriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
          return aipDescriptiveMetadataTabs;
        }
      });
    }

    // Preservation events
    // Check if user has permissions to see the preservation events
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_PRESERVATION_EVENT)) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEventsTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          Filter eventFilter = new Filter(
            new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
          return new SearchWrapper(false).createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(),
            new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseAIP_preservationEvents")
              .withFilter(eventFilter).withSummary(messages.searchResults()).bindOpener()));
        }
      });
    }
    // Logs
    // Check if user has permissions to see the logs'
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_LOG_ENTRY)) {
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
    }

    // Risk incidences
    // Check if user has permissions to see the risk incidences
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_RISK_INCIDENCE)) {
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
    }

    // Disposal
    List<String> disposalMethods = new ArrayList<>();
    disposalMethods.addAll(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD.getMethods());
    disposalMethods.addAll(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE.getMethods());
    if (PermissionClientUtils.hasPermissions(disposalMethods, aip.getPermissions())
      && !aip.getState().equals(AIPState.INGEST_PROCESSING)) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new DisposalPolicyAssociationTab(browseAIPResponse, actionCallback);
        }
      });
    }

    // Permissions
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        AipToolbarActions aipToolbarActions = AipToolbarActions.get(aip.getId(), aip.getState(), aip.getPermissions());
        return new EditPermissionsTab(new ActionableWidgetBuilder<>(aipToolbarActions).buildGroupedListWithObjects(
          new ActionableObject<>(aip), List.of(AipToolbarActions.AIPAction.UPDATE_PERMISSIONS),
          List.of(AipToolbarActions.AIPAction.UPDATE_PERMISSIONS)), IndexedAIP.class.getName(), aip);
      }
    });

    // Details
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DetailsTab(browseAIPResponse);
      }
    });
  }
}