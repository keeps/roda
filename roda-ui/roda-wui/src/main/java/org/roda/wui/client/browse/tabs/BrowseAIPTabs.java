package org.roda.wui.client.browse.tabs;

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
import org.roda.wui.client.common.actions.PreservationEventActions;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationTab;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseAIPTabs extends Tabs {
  public void init(IndexedAIP aip, BrowseAIPResponse browseAIPResponse,
    DescriptiveMetadataInfos descriptiveMetadataInfos) {
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // TODO: These tabs should be lazy loading

    // Descriptive metadata
    AIPDescriptiveMetadataTabs aipDescriptiveMetadataTabs = new AIPDescriptiveMetadataTabs();
    aipDescriptiveMetadataTabs.init(aip, descriptiveMetadataInfos);
    aipDescriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), aipDescriptiveMetadataTabs);

    // Preservation events
    Filter eventFilter = new Filter(new AllFilterParameter());
    eventFilter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    SearchWrapper preservationEvents = new SearchWrapper(false)
      .createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(),
        new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseAIP_preservationEvents").withFilter(eventFilter)
          .withSummary(messages.searchResults()).bindOpener()
          .withActionable(PreservationEventActions.get(aip.getId(), null, null))));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEventsTab()), preservationEvents);

    // Logs
    SearchWrapper auditLogs = new SearchWrapper(false);
    auditLogs.createListAndSearchPanel(new ListBuilder<>(() -> new LogEntryList(),
      new AsyncTableCellOptions<>(LogEntry.class, "BrowseAIP_auditLogs")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aip.getId())))
        .withJustActive(justActive).bindOpener()));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.auditLogsTab()), auditLogs);

    // Risk incidences
    SearchWrapper riskIncidences = new SearchWrapper(false);
    riskIncidences.createListAndSearchPanel(new ListBuilder<>(() -> new RiskIncidenceList(),
      new AsyncTableCellOptions<>(RiskIncidence.class, "BrowseAIP_riskIncidences")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aip.getId())))
        .withJustActive(justActive).bindOpener()));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.risksTab()), riskIncidences);

    // Disposal
    DisposalPolicyAssociationTab disposalPolicyAssociationPanel = new DisposalPolicyAssociationTab(browseAIPResponse);
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalTab()), disposalPolicyAssociationPanel);

    // Permissions
    EditPermissionsTab permissionsTab = new EditPermissionsTab(IndexedAIP.class.getName(), aip);
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), permissionsTab);

  }
}