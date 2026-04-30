package org.roda.wui.client.browse.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.DisposalCreateConfirmationDestroyActions;
import org.roda.wui.client.common.actions.DisposalCreateConfirmationReviewActions;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import java.util.Date;

public class DisposalOverdueRecordsTabs extends Tabs {

  private static final DateTimeFormat formatter = DateTimeFormat.getFormat(RodaConstants.SIMPLE_DATE_FORMATTER);

  private static final Filter SHOW_RECORDS_TO_REVIEW = new Filter(
    new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_ACTION, DisposalActionCode.REVIEW.name()),
    new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name()),
    new DateIntervalFilterParameter(RodaConstants.AIP_OVERDUE_DATE, RodaConstants.AIP_OVERDUE_DATE, null,
      formatter.parse(formatter.format(new Date()))),
    new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLD_STATUS, Boolean.FALSE.toString()),
    new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID));

  private static final Filter SHOW_RECORDS_TO_DESTROY = new Filter(
    new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_ACTION, DisposalActionCode.DESTROY.name()),
    new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name()),
    new DateIntervalFilterParameter(RodaConstants.AIP_OVERDUE_DATE, RodaConstants.AIP_OVERDUE_DATE, null,
      formatter.parse(formatter.format(new Date()))),
    new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLD_STATUS, Boolean.FALSE.toString()),
    new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID));

  private static final Filter SHOW_RECORDS_WITH_RETENTION_PERIOD_ERRORS = new Filter(new SimpleFilterParameter(
    RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION, RetentionPeriodCalculation.ERROR.name()));

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public void init() {

    ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
        .withFilter(SHOW_RECORDS_TO_DESTROY).withActionable(DisposalCreateConfirmationDestroyActions.get())
        .withJustActive(true).bindOpener());

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalConfirmationShowRecordsToDestroy()),
      new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
        }
      });

    ListBuilder<IndexedAIP> overdueRecordsToReviewListBuilder = new ListBuilder<>(
      () -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
        .withFilter(SHOW_RECORDS_TO_REVIEW).withActionable(DisposalCreateConfirmationReviewActions.get())
        .withJustActive(true).bindOpener());

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalConfirmationShowRecordsToReview()),
      new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(overdueRecordsToReviewListBuilder);
        }
      });

    ListBuilder<IndexedAIP> recordsWithErrorListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
        .withFilter(SHOW_RECORDS_WITH_RETENTION_PERIOD_ERRORS).withJustActive(true).bindOpener());

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalConfirmationShowRecordsRetentionPeriodCalculationError()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new SearchWrapper(false).createListAndSearchPanel(recordsWithErrorListBuilder);
      }
    });

  }
}