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
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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
  FlowPanel header;
  @UiField
  ActionsToolbar actionsToolbar;
  @UiField
  SimplePanel informationPanel;
  @UiField(provided = true)
  DisposalConfirmationPanel disposalConfirmationPanel;
  @UiField(provided = true)
  RetentionPeriodPanel retentionPeriodPanel;
  @UiField(provided = true)
  DisposalHoldsPanel disposalHoldsPanel;

  public DisposalPolicyAssociationTab(BrowseAIPResponse response,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    disposalConfirmationPanel = new DisposalConfirmationPanel(response.getIndexedAIP().getDisposalConfirmationId());

    retentionPeriodPanel = new RetentionPeriodPanel(response.getIndexedAIP());

    disposalHoldsPanel = new DisposalHoldsPanel(response.getIndexedAIP());

    initWidget(uiBinder.createAndBindUi(this));

    if (showNotAssigned(response.getIndexedAIP())) {
      informationPanel.addStyleName("table-empty-inner");
      informationPanel.setWidget(getNoItemsToDisplay());
    }

    IndexedAIP aip = response.getIndexedAIP();

    // TOOLBAR
    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setActionableMenu(
      new ActionableWidgetBuilder<IndexedAIP>(AipToolbarActions.get(aip.getId(), aip.getState(), aip.getPermissions()))
        .withActionCallback(actionCallback).buildGroupedListWithObjects(new ActionableObject<>(aip),
          List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
            AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD),
          List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
            AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD)),
      true);
    header.setVisible(actionsToolbar.isVisible());
  }

  private boolean showNotAssigned(IndexedAIP aip) {
    if (StringUtils.isNotBlank(aip.getDisposalConfirmationId())) {
      return false;
    }

    if (aip.isOnHold()) {
      return false;
    }

    return !StringUtils.isNotBlank(aip.getDisposalScheduleId());
  }

  private Label getNoItemsToDisplay() {
    Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.disposalPolicyNoneSummary()));
    label.addStyleName("table-empty-inner-label");

    return label;
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociationTab> {
  }
}
