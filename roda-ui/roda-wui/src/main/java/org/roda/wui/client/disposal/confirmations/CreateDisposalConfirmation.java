package org.roda.wui.client.disposal.confirmations;

import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.wui.client.common.UserLogin;
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
    new DateIntervalFilterParameter(RodaConstants.AIP_OVERDUE_DATE, RodaConstants.AIP_OVERDUE_DATE, null,
      formatter.parse(formatter.format(new Date()))),
    new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID));

  private static final Filter SHOW_RECORDS_TO_DESTROY = new Filter(
    new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_ACTION, DisposalActionCode.DESTROY.name()),
    new DateIntervalFilterParameter(RodaConstants.AIP_OVERDUE_DATE, RodaConstants.AIP_OVERDUE_DATE, null,
      formatter.parse(formatter.format(new Date()))),
    new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID));

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateDisposalConfirmation createDisposalConfirmation = new CreateDisposalConfirmation();
      callback.onSuccess(createDisposalConfirmation);
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

  private static CreateDisposalConfirmation instance = null;
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
  FlowPanel content;

  /**
   * Create a new panel to create a disposal confirmation
   */
  public CreateDisposalConfirmation() {
    ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
        .withFilter(SHOW_RECORDS_TO_DESTROY).withActionable(DisposalCreateConfirmationDestroyActions.get())
        .withJustActive(true).bindOpener());
    overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    configureDisposalAction();

    createDisposalConfirmationDescription.add(new HTMLWidgetWrapper("CreateDisposalConfirmationDescription.html"));
  }

  private void configureDisposalAction() {
    destroyScheduleOpt.setText("Show records to destroy");
    destroyScheduleOpt.addValueChangeHandler(valueChangeEvent -> {
      ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
          .withFilter(SHOW_RECORDS_TO_DESTROY).withActionable(DisposalCreateConfirmationDestroyActions.get())
          .withJustActive(true).bindOpener());
      content.remove(overdueRecordsSearch);
      overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
      content.add(overdueRecordsSearch);
    });

    reviewScheduleOpt.setText("Show records to review");
    reviewScheduleOpt.addValueChangeHandler(valueChangeEvent -> {
      ListBuilder<IndexedAIP> overdueRecordsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalOverdueRecords_aip").withSummary(messages.listOfAIPs())
          .withFilter(SHOW_RECORDS_TO_REVIEW).withActionable(DisposalCreateConfirmationReviewActions.get())
          .withJustActive(true).bindOpener());
      content.remove(overdueRecordsSearch);
      overdueRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(overdueRecordsListBuilder);
      content.add(overdueRecordsSearch);
    });
  }

}
