package org.roda.wui.client.browse.tabs;

import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationPanel;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationTab;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseAIPTabs extends Tabs {
  public void init(IndexedAIP aip, BrowseAIPResponse browseAIPResponse, DescriptiveMetadataInfos descriptiveMetadataInfos) {
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // Descriptive metadata
    AIPDescriptiveMetadataTabs aipDescriptiveMetadataTabs = new AIPDescriptiveMetadataTabs();
    aipDescriptiveMetadataTabs.init(aip, descriptiveMetadataInfos);
    aipDescriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), aipDescriptiveMetadataTabs);

    // Preservation events
    SearchWrapper preservationEvents = new SearchWrapper(false);
    preservationEvents.createListAndSearchPanel(new ListBuilder<>(() -> new PreservationEventList(), new AsyncTableCellOptions<>(IndexedPreservationEvent.class, "BrowseAIP_preservationEvents")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId())))
            .withJustActive(justActive).bindOpener()));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.preservationEventsTab()), preservationEvents);

    // Logs
    SearchWrapper auditLogs = new SearchWrapper(false);
    auditLogs.createListAndSearchPanel(new ListBuilder<>(() -> new LogEntryList(), new AsyncTableCellOptions<>(LogEntry.class, "BrowseAIP_auditLogs")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aip.getId())))
            .withJustActive(justActive).bindOpener()));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.auditLogsTab()), auditLogs);

    // Risk incidences
    SearchWrapper riskIncidences = new SearchWrapper(false);
    riskIncidences.createListAndSearchPanel(new ListBuilder<>(() -> new RiskIncidenceList(), new AsyncTableCellOptions<>(RiskIncidence.class, "BrowseAIP_riskIncidences")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aip.getId())))
            .withJustActive(justActive).bindOpener()));
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.risksTab()), riskIncidences);

    // Disposal
    DisposalPolicyAssociationTab disposalPolicyAssociationPanel = new DisposalPolicyAssociationTab(browseAIPResponse);
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalTab()), disposalPolicyAssociationPanel);
  }
}