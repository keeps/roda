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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.DisposalPolicySummaryPanel;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.utils.DisposalPolicyUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociationTab extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.AIP_TITLE, RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME));
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static DisposalPolicyAssociationTab.MyUiBinder uiBinder = GWT
    .create(DisposalPolicyAssociationTab.MyUiBinder.class);
  @UiField
  ActionsToolbar actionsToolbar;
  @UiField
  DisposalPolicySummaryPanel disposalPolicySummaryPanel;
  @UiField(provided = true)
  DisposalConfirmationPanel disposalConfirmationPanel;
  @UiField(provided = true)
  RetentionPeriodPanel retentionPeriodPanel;
  @UiField(provided = true)
  DisposalHoldsPanel disposalHoldsPanel;
  private IndexedAIP aip;

  public DisposalPolicyAssociationTab(BrowseAIPResponse response,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    disposalConfirmationPanel = new DisposalConfirmationPanel(response.getIndexedAIP().getDisposalConfirmationId());

    retentionPeriodPanel = new RetentionPeriodPanel(response.getIndexedAIP());

    disposalHoldsPanel = new DisposalHoldsPanel(response.getIndexedAIP());

    initWidget(uiBinder.createAndBindUi(this));

    aip = response.getIndexedAIP();

    // TOOLBAR
    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setActionableMenu(
      new ActionableWidgetBuilder<IndexedAIP>(AipToolbarActions.get(aip.getId(), aip.getState(), aip.getPermissions()))
        .withActionCallback(actionCallback).buildGroupedListWithObjects(new ActionableObject<>(aip),
          List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
            AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD),
          List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
            AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD)));

    // DISPOSAL POLICY SUMMARY
    disposalPolicySummaryPanel.setIcon("fas fa-info-circle");
    disposalPolicySummaryPanel.setText(DisposalPolicyUtils.getDisposalPolicySummaryText(aip));

  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociationTab> {
  }
}
