/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateActionJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskRegister extends Composite {

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
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "riskregister";
    }
  };

  private static RiskRegister instance = null;

  interface MyUiBinder extends UiBinder<Widget, RiskRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label riskRegisterTitle;

  @UiField
  FlowPanel riskRegisterDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  RiskList riskList;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  @UiField
  Button buttonRefresh;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(IndexedRisk.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(IndexedRisk.class.getName());

  /**
   * Create a risk register page
   *
   * @param user
   */
  public RiskRegister() {
    riskList = new RiskList("RiskRegister_risks", Filter.NULL, messages.risksTitle(), true);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.riskRegisterSearchPlaceHolder(), false,
      false, false);
    searchPanel.setList(riskList);

    riskList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedRisk selected = riskList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    riskList.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedRisk>() {
      @Override
      public void onSelectionChange(SelectedItems<IndexedRisk> selected) {
        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
        buttonRemove.setEnabled(!empty);
        startProcess.setEnabled(!empty);
      }
    });

    initWidget(uiBinder.createAndBindUi(this));
    riskRegisterDescription.add(new HTMLWidgetWrapper("RiskRegisterDescription.html"));
    buttonRemove.setEnabled(false);
    startProcess.setEnabled(false);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }
    };

    inputDateInitial.setFormat(dateFormat);
    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
    inputDateInitial.setFireNullValues(true);
    inputDateInitial.addValueChangeHandler(valueChangeHandler);
    inputDateInitial.setTitle(messages.dateIntervalLabelInitial());

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);
    inputDateFinal.setTitle(messages.dateIntervalLabelFinal());

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RiskRegister getInstance() {
    if (instance == null) {
      instance = new RiskRegister();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.RISK_IDENTIFIED_ON,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    riskList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      riskList.setFilter(Filter.ALL);
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowRisk.RESOLVER.getHistoryToken())) {
      ShowRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateRisk.RESOLVER.getHistoryToken())) {
      CreateRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditRisk.RESOLVER.getHistoryToken())) {
      EditRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(RiskHistory.RESOLVER.getHistoryToken())) {
      RiskHistory.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateActionJob.RESOLVER.getHistoryToken())) {
      CreateActionJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonAdd")
  void buttonAddRiskHandler(ClickEvent e) {
    HistoryUtils.newHistory(RESOLVER, CreateRisk.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonRefresh")
  void buttonRefreshRiskHandler(ClickEvent e) {
    BrowserService.Util.getInstance().updateRiskCounters(new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        riskList.refresh();
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogRefresh(), messages.riskRefreshDone());
        riskList.refresh();
      }
    });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveRiskHandler(ClickEvent e) {
    final SelectedItems<IndexedRisk> selected = riskList.getSelected();

    ClientSelectedItemsUtils.size(IndexedRisk.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.riskRemoveFolderConfirmDialogTitle(),
          messages.riskRemoveSelectedConfirmDialogMessage(size), messages.riskRemoveFolderConfirmDialogCancel(),
          messages.riskRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().deleteRisk(selected, new AsyncCallback<Job>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                  }

                  @Override
                  public void onSuccess(Job result) {
                    Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        Timer timer = new Timer() {
                          @Override
                          public void run() {
                            Toast.showInfo(messages.riskRemoveSuccessTitle(), messages.riskRemoveSuccessMessage(size));
                            riskList.refresh();
                          }
                        };

                        timer.schedule(RodaConstants.ACTION_TIMEOUT);
                      }

                      @Override
                      public void onSuccess(final Void nothing) {
                        HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                      }
                    });
                  }
                });
              }
            }
          });
      }
    });
  }

  @UiHandler("startProcess")
  void handleButtonProcess(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(riskList.getSelected());
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

}
