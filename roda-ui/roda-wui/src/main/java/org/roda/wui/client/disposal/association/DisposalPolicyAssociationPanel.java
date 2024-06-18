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
import org.roda.wui.client.common.NavigationToolbar;
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

public class DisposalPolicyAssociationPanel extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.AIP_TITLE, RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME));
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public String getHistoryToken() {
      return "association";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(1);

        Services service = new Services("Create AIP breadcrumb", "get");
        service.rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()),
          IndexedAIP.class).whenComplete((aip, error) -> { // get aip
            // ancestors
            if (error != null) {
              if (error instanceof NotFoundException) {
                Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
                HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(error);
              }
            } else {
              CompletableFuture<List<IndexedAIP>> futureAncestors = service.aipResource(s -> s.getAncestors(aipId));

              CompletableFuture<List<String>> futureRepFields = service
                .aipResource(AIPRestService::retrieveAIPRuleProperties);

              CompletableFuture<DescriptiveMetadataInfos> futureDescriptiveMetadataInfos = service
                .aipResource(s -> s.getDescriptiveMetadata(aipId, LocaleInfo.getCurrentLocale().getLocaleName()));

              CompletableFuture<LongResponse> futureChildAipCount = service.rodaEntityRestService(
                s -> s.count(new FindRequest.FindRequestBuilder(
                  new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId)), false).build()),
                IndexedAIP.class);

              CompletableFuture<LongResponse> futureRepCount = service.rodaEntityRestService(
                s -> s.count(new CountRequest(
                  new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId)), false)),
                IndexedRepresentation.class);

              CompletableFuture<LongResponse> futureDipCount = service.rodaEntityRestService(
                s -> s.count(
                  new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_IDS, aipId)), false)),
                IndexedDIP.class);

              CompletableFuture<LongResponse> futureIncidenceCount = service.rodaEntityRestService(
                s -> s.count(new CountRequest(
                  new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId)), false)),
                RiskIncidence.class);

              CompletableFuture<LongResponse> futureEventCount = service.rodaEntityRestService(
                s -> s.count(new CountRequest(
                  new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId)), false)),
                IndexedPreservationEvent.class);

              CompletableFuture<LongResponse> futureLogCount = service.rodaEntityRestService(
                s -> s.count(new CountRequest(
                  new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aipId)), false)),
                LogEntry.class);

              CompletableFuture.allOf(futureChildAipCount, futureRepCount, futureDipCount, futureAncestors,
                futureAncestors, futureRepFields, futureDescriptiveMetadataInfos, futureIncidenceCount,
                futureEventCount, futureLogCount).thenApply(v -> {
                  BrowseAIPResponse rp = new BrowseAIPResponse();
                  rp.setIndexedAIP(aip);
                  rp.setAncestors(futureAncestors.join());
                  rp.setRepresentationInformationFields(futureRepFields.join());
                  rp.setDescriptiveMetadataInfos(futureDescriptiveMetadataInfos.join());
                  rp.setChildAipsCount(futureChildAipCount.join());
                  rp.setRepresentationCount(futureRepCount.join());
                  rp.setDipCount(futureDipCount.join());
                  rp.setIncidenceCount(futureIncidenceCount.join());
                  rp.setEventCount(futureEventCount.join());
                  rp.setLogCount(futureLogCount.join());
                  return rp;
                }).whenComplete((value, throwable) -> {

                  if (throwable == null) {
                    callback.onSuccess(new DisposalPolicyAssociationPanel(value));
                  }
                });
            }
          });

      }
    }
  };
  private static DisposalPolicyAssociationPanel.MyUiBinder uiBinder = GWT
    .create(DisposalPolicyAssociationPanel.MyUiBinder.class);
  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;
  @UiField
  TitlePanel titlePanel;
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
  public DisposalPolicyAssociationPanel(BrowseAIPResponse response) {
    disposalConfirmationPanel = new DisposalConfirmationPanel(response.getIndexedAIP().getDisposalConfirmationId());

    retentionPeriodPanel = new RetentionPeriodPanel(response.getIndexedAIP());

    disposalHoldsPanel = new DisposalHoldsPanel(response.getIndexedAIP());

    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(DisposalAssociationActions.get());

    aip = response.getIndexedAIP();
    titlePanel.setText(aip.getTitle());
    titlePanel.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));

    // NAVIGATION TOOLBAR
    navigationToolbar.withObject(aip);
    navigationToolbar.withoutButtons();
    navigationToolbar.withPermissions(aip.getPermissions());
    navigationToolbar.build();

    // DISPOSAL POLICY SUMMARY
    disposalPolicySummaryPanel.setIcon("fas fa-info-circle");
    disposalPolicySummaryPanel.setText(DisposalPolicyUtils.getDisposalPolicySummaryText(aip));

    BreadcrumbItem item = new BreadcrumbItem(messages.disposalPolicyTitle(),
      () -> HistoryUtils.newHistory(DisposalPolicyAssociationPanel.RESOLVER, aip.getId()));
    List<BreadcrumbItem> aipBreadcrumbs = BreadcrumbUtils.getAipBreadcrumbs(response.getAncestors(),
      response.getIndexedAIP());
    aipBreadcrumbs.add(item);
    navigationToolbar.updateBreadcrumb(response.getIndexedAIP(), response.getAncestors());
    navigationToolbar.updateBreadcrumbPath(aipBreadcrumbs);

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociationPanel> {
  }
}
