/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.DisposalPolicySummaryPanel;
import org.roda.wui.client.common.NavigationToolbarLegacy;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalAssociationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.DisposalPolicyUtils;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociationTab extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.AIP_TITLE, RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME));
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static DisposalPolicyAssociationTab.MyUiBinder uiBinder = GWT
    .create(DisposalPolicyAssociationTab.MyUiBinder.class);
  @UiField
  FlowPanel content;
  @UiField
  DisposalPolicySummaryPanel disposalPolicySummaryPanel;
  @UiField(provided = true)
  DisposalConfirmationPanel disposalConfirmationPanel;
  @UiField(provided = true)
  RetentionPeriodPanel retentionPeriodPanel;
  @UiField(provided = true)
  DisposalHoldsPanel disposalHoldsPanel;
  @UiField
  SimplePanel actionsSidebar;
  ActionableWidgetBuilder<IndexedAIP> actionableWidgetBuilder;
  private IndexedAIP aip;
  public DisposalPolicyAssociationTab(BrowseAIPResponse response) {
    disposalConfirmationPanel = new DisposalConfirmationPanel(response.getIndexedAIP().getDisposalConfirmationId());

    retentionPeriodPanel = new RetentionPeriodPanel(response.getIndexedAIP());

    disposalHoldsPanel = new DisposalHoldsPanel(response.getIndexedAIP());

    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(DisposalAssociationActions.get());

    aip = response.getIndexedAIP();

    // DISPOSAL POLICY SUMMARY
    disposalPolicySummaryPanel.setIcon("fas fa-info-circle");
    disposalPolicySummaryPanel.setText(DisposalPolicyUtils.getDisposalPolicySummaryText(aip));

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociationTab> {
  }
}
