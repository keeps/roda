package org.roda.wui.client.disposal.confirmations;

import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodCalculation;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisposalCreateConfirmationDestroyActions;
import org.roda.wui.client.common.actions.DisposalCreateConfirmationReviewActions;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateDisposalConfirmation extends Composite {

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

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalConfirmations.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_confirmation";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalConfirmation> {
  }

  private static CreateDisposalConfirmation.MyUiBinder uiBinder = GWT
    .create(CreateDisposalConfirmation.MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper overdueRecordsSearch;

  @UiField
  FlowPanel createDisposalConfirmationDescription;

  @UiField
  RadioButton destroyScheduleOpt;

  @UiField
  RadioButton reviewScheduleOpt;

  @UiField
  RadioButton retentionCalculationFailedOpt;

  @UiField
  FlowPanel content;

  private static CreateDisposalConfirmation instance;
  private SelectedItems<IndexedAIP> selected = null;

  private final AsyncCallback<Actionable.ActionImpact> listActionableCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact impact) {
      if (impact.equals(Actionable.ActionImpact.UPDATED)) {
        selected = overdueRecordsSearch.getSelectedItems(IndexedAIP.class);
      }
    }
  };

  public static CreateDisposalConfirmation getInstance() {
    if (instance == null) {
      instance = new CreateDisposalConfirmation();
    }

    return instance;
  }

  public SelectedItems<IndexedAIP> getSelected() {
    return selected;
  }

  public void clear() {
    instance = null;
    selected = null;
  }

  /**
   * Create a new panel to create a disposal confirmation
   */
  private CreateDisposalConfirmation() {
    ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
        .withFilter(SHOW_RECORDS_TO_DESTROY).withActionable(DisposalCreateConfirmationDestroyActions.get())
        .withActionableCallback(listActionableCallback).withJustActive(true).bindOpener());
    overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    configureDisposalAction();

    createDisposalConfirmationDescription.add(new HTMLWidgetWrapper("CreateDisposalConfirmationDescription.html"));
  }

  private void configureDisposalAction() {
    destroyScheduleOpt.setText(messages.disposalConfirmationShowRecordsToDestroy());
    destroyScheduleOpt.addValueChangeHandler(valueChangeEvent -> {
      ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
          .withFilter(SHOW_RECORDS_TO_DESTROY).withActionable(DisposalCreateConfirmationDestroyActions.get())
          .withActionableCallback(listActionableCallback).withJustActive(true).bindOpener());
      content.remove(overdueRecordsSearch);
      overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
      content.add(overdueRecordsSearch);
    });

    reviewScheduleOpt.setText(messages.disposalConfirmationShowRecordsToReview());
    reviewScheduleOpt.addValueChangeHandler(valueChangeEvent -> {
      ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
          .withFilter(SHOW_RECORDS_TO_REVIEW).withActionable(DisposalCreateConfirmationReviewActions.get())
          .withActionableCallback(listActionableCallback).withJustActive(true).bindOpener());
      content.remove(overdueRecordsSearch);
      overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
      content.add(overdueRecordsSearch);
    });

    retentionCalculationFailedOpt.setText(messages.disposalConfirmationShowRecordsRetentionPeriodCalculationError());
    retentionCalculationFailedOpt.addValueChangeHandler(valueChangeEvent -> {
      ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
          .withFilter(SHOW_RECORDS_WITH_RETENTION_PERIOD_ERRORS).withJustActive(true).bindOpener());
      content.remove(overdueRecordsSearch);
      overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
      content.add(overdueRecordsSearch);
    });
  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else {
      String basePage = historyTokens.remove(0);
      if (CreateDisposalConfirmationDataPanel.RESOLVER.getHistoryToken().equals(basePage)) {
        CreateDisposalConfirmationDataPanel.RESOLVER.resolve(historyTokens, callback);
      }
    }
  }
}
